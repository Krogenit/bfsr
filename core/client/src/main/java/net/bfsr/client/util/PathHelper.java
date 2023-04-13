package net.bfsr.client.util;

import java.io.File;

public final class PathHelper {
    public static final File CONTENT = new File(".." + File.separator + "..", "core" + File.separator + "client" + File.separator + "src" + File.separator + "main" + File.separator + "resources");
    public static final File TEXTURE = new File(CONTENT, "texture");
    public static final File SOUND = new File(CONTENT, "sound");
    public static final File SHADER = new File(CONTENT, "shader");
    public static final File FONT = new File(CONTENT, "font");
    public static final File CONFIG = new File(CONTENT, "config");

    private static final char FILE_NAME_TYPE_SEPARATOR = '.';

    public static String convertPath(String simplePath) {
        return CONTENT + File.separator + simplePath.replace("/", File.separator);
    }

    public static String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.indexOf(FILE_NAME_TYPE_SEPARATOR));
    }
}