package net.bfsr.config.component.cargo;

import net.bfsr.engine.config.Configurable;

@Configurable
public record CargoConfig(int maxCapacity) {}