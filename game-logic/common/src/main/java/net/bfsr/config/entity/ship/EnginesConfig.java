package net.bfsr.config.entity.ship;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.config.Configurable;

import java.util.List;

@Configurable
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnginesConfig {
    private List<EngineConfig> engines;
}