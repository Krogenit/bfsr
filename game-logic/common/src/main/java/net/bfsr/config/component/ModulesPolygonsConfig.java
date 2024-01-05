package net.bfsr.config.component;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.Configurable;
import net.bfsr.config.PolygonConfigurable;
import net.bfsr.config.entity.ship.EnginesConfig;
import net.bfsr.math.Direction;

import java.util.Map;

@Setter
@Getter
@Configurable
public class ModulesPolygonsConfig {
    private PolygonConfigurable reactor;
    private PolygonConfigurable shield;
    private Map<Direction, EnginesConfig> engines;
}