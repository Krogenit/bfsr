package net.bfsr.config.component.crew;

import net.bfsr.engine.config.Configurable;

@Configurable
public record CrewConfig(int maxCapacity) {}