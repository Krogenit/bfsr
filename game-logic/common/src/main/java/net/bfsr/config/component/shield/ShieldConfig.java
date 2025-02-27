package net.bfsr.config.component.shield;

import net.bfsr.engine.config.Configurable;

@Configurable
public record ShieldConfig(
        float maxShield,
        float regenInSeconds,
        float rebuildTimeInSeconds
) {}