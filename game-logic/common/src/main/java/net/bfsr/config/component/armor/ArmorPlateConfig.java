package net.bfsr.config.component.armor;

import net.bfsr.config.Configurable;

@Configurable
public record ArmorPlateConfig(
        String name,
        float maxArmorValue,
        float regenSpeedInSeconds,
        float hullProtection
) {}