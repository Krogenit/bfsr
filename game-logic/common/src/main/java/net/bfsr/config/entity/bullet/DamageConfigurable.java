package net.bfsr.config.entity.bullet;

import net.bfsr.engine.config.Configurable;

@Configurable
public record DamageConfigurable(float armor, float hull, float shield) {}