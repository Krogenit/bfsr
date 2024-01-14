package net.bfsr.config.entity.bullet;

import net.bfsr.config.Configurable;

@Configurable
public record DamageConfigurable(float armor, float hull, float shield) {}