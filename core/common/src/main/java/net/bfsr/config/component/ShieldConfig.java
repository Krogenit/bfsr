package net.bfsr.config.component;

import net.bfsr.config.Configurable;

@Configurable
public record ShieldConfig(
        String name,
        String texture,
        float maxShield,
        float shieldRegen,
        float rebuildTime
) {}