package net.bfsr.config.entity.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.PolygonConfigurable;
import net.bfsr.engine.config.Vector2fConfigurable;

import java.util.List;

@Setter
@Getter
@Configurable
public class EngineConfig {
    private List<PolygonConfigurable> polygons;
    private Vector2fConfigurable effectPosition;
}