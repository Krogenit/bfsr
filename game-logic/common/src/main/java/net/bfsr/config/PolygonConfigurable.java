package net.bfsr.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Configurable
public class PolygonConfigurable {
    private Vector2fConfigurable[] vertices;
}