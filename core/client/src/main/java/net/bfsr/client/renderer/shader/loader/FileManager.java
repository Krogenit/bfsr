package net.bfsr.client.renderer.shader.loader;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.util.PathHelper;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
@Setter
public class FileManager {
    private final List<IncludeEntry> includes = new ArrayList<>();
    private boolean lineMarkers = true;
    private boolean handleIncludePasting = true;

    public IncludeID registerInclude(String filename) {
        includes.add(new IncludeEntry(filename));
        return new IncludeID(includes.size() - 1);
    }

    private String getIncludeContent(IncludeID idx, FoundFile foundFile) {
        IncludeEntry entry = includes.get(idx.getValue());
        foundFile.setFilename(entry.getFilename());

        if (!entry.getContent().isEmpty()) {
            return entry.getContent();
        }

        String content = loadFile(entry.getFilename());
        return content.isEmpty() ? entry.getContent() : content;
    }

    public String manualInclude(String filename, FoundFile foundFile, String prepend, AtomicBoolean foundVersion) {
        String source = getContent(filename, foundFile);
        return manualIncludeText(source, foundFile.getFilename(), prepend, foundVersion);
    }

    private String manualIncludeText(String sourceText, String textFilename, String prepend, AtomicBoolean foundVersion) {
        if (sourceText.isEmpty()) {
            return "";
        }

        StringBuilder text = new StringBuilder();

        text.append(prepend);
        if (lineMarkers) {
            text.append(markerString(1, textFilename, 0));
        }

        int lineCount = 0;
        String[] strings = sourceText.split("\n");
        for (int i = 0; i < strings.length; i++) {
            String line = strings[i];
            lineCount++;

            // Version
            int offset = line.indexOf("#version");
            if (offset != -1) {
                int commentOffset = line.indexOf("//");
                if (commentOffset != -1 && commentOffset < offset)
                    continue;

                if (foundVersion.get()) {
                    // someone else already set the version, so just comment out
                    text.append("//").append(line).append("\n");
                } else {
                    // Reorder so that the #version line is always the first of a shader text
                    String prevText = text.toString();
                    text.setLength(0);
                    text.append(line).append("\n").append(prevText).append("//").append(line).append("\n");
                    foundVersion.set(true);
                }
                continue;
            }

            // Handle replacing #include with text if configured to do so.
            // Otherwise just insert the #include command verbatim, for shaderc to handle.
            if (handleIncludePasting) {
                offset = line.indexOf("#include");
                if (offset != -1) {
                    int commentOffset = line.indexOf("//");
                    if (commentOffset != -1 && commentOffset < offset)
                        continue;

                    int firstQuote = line.indexOf("\"", offset);
                    int secondQuote = line.indexOf("\"", firstQuote + 1);

                    String include = line.substring(firstQuote + 1, secondQuote);

                    FoundFile includeFound = new FoundFile();
                    String includeContent = manualInclude(include, includeFound, "", foundVersion);

                    if (!includeContent.isEmpty()) {
                        text.append(includeContent);
                        if (lineMarkers) {
                            text.append("\n").append(markerString(lineCount + 1, textFilename, 0));
                        }
                    }
                    continue; // Skip adding the original #include line.
                }
            }

            text.append(line).append("\n");
        }

        return text.toString();
    }

    private String getContent(String filename, FoundFile foundFile) {
        if (filename.isEmpty()) {
            return "";
        }

        IncludeID idx = findInclude(filename);

        if (idx.isValid()) {
            return getIncludeContent(idx, foundFile);
        }

        foundFile.setFilename(filename);
        return loadFile(filename);
    }

    private IncludeID findInclude(String filename) {
        for (int i = 0; i < includes.size(); ++i) {
            if (includes.get(i).getFilename().equals(filename)) {
                return new IncludeID(i);
            }
        }

        return new IncludeID();
    }

    private String markerString(int line, String filename, int fileId) {
        if (GL.getCapabilities().GL_ARB_shading_language_include) {
            StringBuilder fixedName = new StringBuilder();
            if (Platform.get() == Platform.WINDOWS) {
                for (int i = 0; i < filename.length(); i++) {
                    char c = filename.charAt(i);
                    if (c == '/' || c == '\\') {
                        fixedName.append("\\\\");
                    } else {
                        fixedName.append(c);
                    }
                }
            } else {
                fixedName.append(fixedName);
            }
            return String.format("#line %d \"", line) + fixedName + "\"\n";
        } else {
            return String.format("#line %d %d\n", line, fileId);
        }
    }

    private String loadFile(String fileName) {
        try {
            return Files.readString(PathHelper.SHADER.resolve(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not load shader " + fileName, e);
        }
    }
}