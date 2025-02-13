package net.bfsr.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class GameObjectConfig extends Config {
    private String texture;
    private Vector2fConfigurable size;
    private Vector2fConfigurable[] vertices;
    private float minDistanceBetweenVerticesSq;
}