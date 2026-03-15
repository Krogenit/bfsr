package net.bfsr.engine.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Configurable
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PolygonConfigurable {
    private Vector2fConfigurable[] vertices;
}