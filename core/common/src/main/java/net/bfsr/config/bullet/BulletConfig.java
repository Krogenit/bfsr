package net.bfsr.config.bullet;

import net.bfsr.config.ColorConfigurable;
import net.bfsr.config.Configurable;
import net.bfsr.config.Vector2fConfigurable;

@Configurable
public record BulletConfig(
        String name,
        float speed,
        float lifeTime,
        Vector2fConfigurable size,
        String texture,
        BulletDamageConfigurable damage,
        ColorConfigurable color,
        Vector2fConfigurable[] vertices
) {
}