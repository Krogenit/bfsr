package net.bfsr.config.entity.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.config.Configurable;

import java.util.List;

@Setter
@Getter
@Configurable
public class EnginesConfig {
    private List<EngineConfig> engines;
}