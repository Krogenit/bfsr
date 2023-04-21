package net.bfsr.config.bullet;

import net.bfsr.config.Configurable;

@Configurable
public record BulletDamageConfigurable(float armor, float hull, float shield) {
}