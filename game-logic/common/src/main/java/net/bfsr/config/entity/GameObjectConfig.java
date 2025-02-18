package net.bfsr.config.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.Config;
import net.bfsr.config.Configurable;
import net.bfsr.config.Vector2fConfigurable;

@Getter
@Setter
@Configurable
public class GameObjectConfig extends Config {
    private String texture;
    private Vector2fConfigurable size;
    private Vector2fConfigurable[] vertices;
}