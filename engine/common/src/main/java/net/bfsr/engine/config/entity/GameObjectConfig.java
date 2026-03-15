package net.bfsr.engine.config.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.config.Config;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.Vector2fConfigurable;

@Getter
@Setter
@Configurable
@NoArgsConstructor
@AllArgsConstructor
public class GameObjectConfig extends Config {
    private String texture;
    private Vector2fConfigurable size;
    private Vector2fConfigurable[] vertices;
}