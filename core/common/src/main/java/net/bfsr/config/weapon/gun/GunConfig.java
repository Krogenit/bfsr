package net.bfsr.config.weapon.gun;

import net.bfsr.config.ColorConfigurable;
import net.bfsr.config.Configurable;
import net.bfsr.config.ConfigurableSound;
import net.bfsr.config.Vector2fConfigurable;

@Configurable
public record GunConfig(
        String name,
        float reloadTimeInSeconds,
        float energyCost,
        String bulletData,
        Vector2fConfigurable size,
        String texture,
        ColorConfigurable color,
        ConfigurableSound[] sounds,
        Vector2fConfigurable[] vertices
) {
}