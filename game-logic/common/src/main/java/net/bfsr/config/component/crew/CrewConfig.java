package net.bfsr.config.component.crew;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.bfsr.engine.config.Configurable;

@Configurable
@Accessors(fluent = true)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrewConfig {
    private int maxCapacity;
}