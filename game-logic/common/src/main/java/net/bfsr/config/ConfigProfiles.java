package net.bfsr.config;

import net.bfsr.GameplayMode;
import net.bfsr.engine.util.PathHelper;

import java.nio.file.Path;
import java.util.Locale;

public final class ConfigProfiles {
    private static final String PROFILES_FOLDER = "profiles";

    private ConfigProfiles() {}

    public static Path getOverlayRoot(GameplayMode mode) {
        String profile = mode.name().toLowerCase(Locale.ROOT);
        return PathHelper.CONFIG.resolve(PROFILES_FOLDER).resolve(profile);
    }
}
