package net.bfsr.client.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class PathHelper {
    public static final Path CONTENT = Path.of("../../core/client/src/main/resources");
    public static final Path SHADER = CONTENT.resolve("shader");
    public static final Path FONT = CONTENT.resolve("font");
    public static final Path CONFIG = CONTENT.resolve("config");

    private static final char FILE_NAME_TYPE_SEPARATOR = '.';

    public static Path convertPath(String simplePath) {
        return CONTENT.resolve(simplePath);
    }

    public static String convertToLocalPath(String path) throws IOException {
        return path.replace(CONTENT.toFile().getCanonicalPath(), "").replace(File.separator, "/").substring(1);
    }

    public static String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.indexOf(FILE_NAME_TYPE_SEPARATOR));
    }
}