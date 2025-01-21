package net.bfsr.engine.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class PathHelper {
    private static final String ROOT_PATH = System.getProperty("assets.path", ".");

    public static final Path CLIENT_CONTENT = Path.of(ROOT_PATH + "assets/client");
    private static final Path CONTENT = Path.of(ROOT_PATH + "assets/common");
    public static final Path CONFIG = CONTENT.resolve("config");
    public static final Path SHADER = CLIENT_CONTENT.resolve("shader");
    public static final Path FONT = CLIENT_CONTENT.resolve("font");

    private static final char FILE_NAME_TYPE_SEPARATOR = '.';

    public static Path convertPath(String simplePath) {
        return CLIENT_CONTENT.resolve(simplePath);
    }

    public static String convertToLocalPath(Path path) {
        return convertToLocalPath(CLIENT_CONTENT, path);
    }

    public static String convertToLocalPath(String canonicalPath) throws IOException {
        return canonicalPath.replace(CLIENT_CONTENT.toFile().getCanonicalPath(), "").replace(File.separator, "/").substring(1);
    }

    public static String convertToLocalPath(Path folder, Path path) {
        return path.toString().replace(folder.toString(), "").replace(File.separator, "/").substring(1);
    }

    public static String getLocalFileFolderPath(Path folder, Path filePath) {
        String filePathString = filePath.toString();
        String replace = filePathString.substring(0, filePathString.lastIndexOf(File.separator)).replace(folder.toString(), "")
                .replace(File.separator, "/");

        if (replace.isEmpty()) {
            return replace;
        } else {
            return replace.substring(1);
        }
    }

    public static String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.indexOf(FILE_NAME_TYPE_SEPARATOR));
    }
}