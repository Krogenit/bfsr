package net.bfsr.config.component.reactor;

import net.bfsr.config.Configurable;

@Configurable
public record ReactorConfig(
        String name,
        float maxEnergyCapacity,
        float regenAmountInSeconds
) {}