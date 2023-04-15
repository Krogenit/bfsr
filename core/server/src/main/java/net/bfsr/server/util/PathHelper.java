package net.bfsr.server.util;

import java.nio.file.Path;

public final class PathHelper {
    public static final Path CONTENT = Path.of("../../core/server/src/main/resources");
    public static final Path CONFIG = CONTENT.resolve("config");
}