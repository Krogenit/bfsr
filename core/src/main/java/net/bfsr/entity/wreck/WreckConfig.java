package net.bfsr.entity.wreck;

import lombok.Getter;
import net.bfsr.config.Configurable;
import net.bfsr.config.Vector2fConfigurable;

@Getter
@Configurable
public class WreckConfig {
    private String texturePath, fireTexturePath, sparkleTexturePath;
    private Vector2fConfigurable[] vertices;
}
