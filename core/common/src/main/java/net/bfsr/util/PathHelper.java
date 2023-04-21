package net.bfsr.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class PathHelper {
    public static final Path CLIENT_CONTENT = Path.of("../../core/client/src/main/resources");
    public static final Path CONTENT = Path.of("../../core/common/src/main/resources");
    public static final Path CLIENT_CONFIG = CLIENT_CONTENT.resolve("config");
    public static final Path CONFIG = CONTENT.resolve("config");
    public static final Path SHADER = CLIENT_CONTENT.resolve("shader");
    public static final Path FONT = CLIENT_CONTENT.resolve("font");

    private static final char FILE_NAME_TYPE_SEPARATOR = '.';

    public static Path convertPath(String simplePath) {
        return CLIENT_CONTENT.resolve(simplePath);
    }

    public static String convertToLocalPath(String path) throws IOException {
        return path.replace(CLIENT_CONTENT.toFile().getCanonicalPath(), "").replace(File.separator, "/").substring(1);
    }

    public static String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.indexOf(FILE_NAME_TYPE_SEPARATOR));
    }
}