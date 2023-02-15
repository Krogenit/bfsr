package net.bfsr.client.util;

import java.io.File;

public final class PathHelper {
    public static final File CONTENT = new File("..\\..\\", "core/client/src/main/resources");
    public static final File TEXTURE = new File(CONTENT, "texture");
    public static final File SOUND = new File(CONTENT, "sound");
    public static final File SHADER = new File(CONTENT, "shader");
    public static final File FONT = new File(CONTENT, "font");
    public static final File CONFIG = new File(CONTENT, "config");
}
