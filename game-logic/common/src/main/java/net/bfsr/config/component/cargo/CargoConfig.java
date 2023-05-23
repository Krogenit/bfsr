package net.bfsr.config.component.cargo;

import net.bfsr.config.Configurable;

@Configurable
public record CargoConfig(
        String name,
        int maxCapacity
) {}