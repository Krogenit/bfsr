package net.bfsr.config.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.config.entity.ship.EnginesConfig;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.PolygonConfigurable;
import net.bfsr.engine.math.Direction;

import java.util.Map;

@Configurable
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ModulesPolygonsConfig {
    private PolygonConfigurable reactor;
    private PolygonConfigurable shield;
    private Map<Direction, EnginesConfig> engines;
}