package net.bfsr.config.entity.ship;

import net.bfsr.config.ColorConfigurable;
import net.bfsr.config.Configurable;
import net.bfsr.config.Vector2fConfigurable;

@Configurable
public record ShipConfig(
        String name,
        Vector2fConfigurable size,
        float destroyTimeInSeconds,
        String texture,
        String damageTexture,
        ColorConfigurable effectsColor,
        Vector2fConfigurable[] weaponSlotPositions,
        Vector2fConfigurable[] vertices
) {}