package net.bfsr.config.component.shield;

import net.bfsr.config.Configurable;

@Configurable
public record ShieldConfig(
        String texture,
        float maxShield,
        float regenInSeconds,
        float rebuildTimeInSeconds
) {}