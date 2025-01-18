package net.bfsr.engine.renderer.shader.loader;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.util.IOUtils;
import net.bfsr.engine.util.PathHelper;
import org.lwjgl.system.Platform;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.bfsr.engine.renderer.shader.ShaderManager.GL_ARB_SHADING_LANGUAGE_INCLUDE;

@Log4j2
@Setter
public class FileManager {
    private boolean lineMarkers = true;
    private boolean handleIncludePasting = !GL_ARB_SHADING_LANGUAGE_INCLUDE;

    public String manualInclude(String filename, FoundFile foundFile, String prepend, AtomicBoolean foundVersion) {
        String source = getContent(filename, foundFile);
        return manualIncludeText(source, foundFile.getFilename(), prepend, foundVersion);
    }

    private String manualIncludeText(String sourceText, String textFilename, String prepend, AtomicBoolean foundVersion) {
        if (sourceText.isEmpty()) {
            return "";
        }

        StringBuilder text = new StringBuilder(256);

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
                if (commentOffset != -1 && commentOffset < offset) {
                    continue;
                }

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
                    if (commentOffset != -1 && commentOffset < offset) {
                        continue;
                    }

                    int firstQuote = line.indexOf('"', offset);
                    int secondQuote = line.indexOf('"', firstQuote + 1);

                    String include = line.substring(firstQuote + 2, secondQuote);

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

        foundFile.setFilename(filename);
        return IOUtils.readFile(PathHelper.SHADER.resolve(filename));
    }

    private String markerString(int line, String filename, int fileId) {
        if (GL_ARB_SHADING_LANGUAGE_INCLUDE) {
            StringBuilder fixedName = new StringBuilder(64);
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
}