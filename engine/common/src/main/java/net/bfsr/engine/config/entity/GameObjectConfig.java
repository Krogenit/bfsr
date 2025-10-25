package net.bfsr.engine.config.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.config.Config;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.Vector2fConfigurable;

@Getter
@Setter
@Configurable
public class GameObjectConfig extends Config {
    private String texture;
    private Vector2fConfigurable size;
    private Vector2fConfigurable[] vertices;
}