package net.bfsr.config.weapon.beam;

import net.bfsr.config.ColorConfigurable;
import net.bfsr.config.Configurable;
import net.bfsr.config.ConfigurableSound;
import net.bfsr.config.Vector2fConfigurable;
import net.bfsr.config.bullet.DamageConfigurable;

@Configurable
public record BeamConfig(
        String name,
        ConfigurableSound[] sounds,
        float reloadTimeInSeconds,
        float energyCost,
        Vector2fConfigurable size,
        float beamMaxRange,
        DamageConfigurable damage,
        String texture,
        ColorConfigurable color,
        Vector2fConfigurable[] vertices
) {
}