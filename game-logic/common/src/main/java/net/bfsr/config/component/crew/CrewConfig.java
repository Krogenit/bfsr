package net.bfsr.config.component.crew;

import net.bfsr.config.Configurable;

@Configurable
public record CrewConfig(
        int maxCapacity
) {}