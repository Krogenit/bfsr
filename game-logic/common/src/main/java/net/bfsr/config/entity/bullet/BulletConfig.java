package net.bfsr.config.entity.bullet;

import net.bfsr.config.ColorConfigurable;
import net.bfsr.config.Configurable;
import net.bfsr.config.Vector2fConfigurable;

@Configurable
public record BulletConfig(
        String name,
        float speed,
        float lifeTimeInSeconds,
        Vector2fConfigurable size,
        String texture,
        DamageConfigurable damage,
        ColorConfigurable color,
        Vector2fConfigurable[] vertices
) {
}