package net.bfsr.config.entity.wreck;

import net.bfsr.config.Configurable;
import net.bfsr.config.Vector2fConfigurable;
import net.bfsr.entity.wreck.WreckType;

@Configurable
public record WreckConfig(
        String name,
        WreckType type,
        String texturePath,
        String fireTexturePath,
        String sparkleTexturePath,
        Vector2fConfigurable[] vertices
) {}