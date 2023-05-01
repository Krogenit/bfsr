package net.bfsr.config.component.hull;

import net.bfsr.config.Configurable;

@Configurable
public record HullConfig(
        String name,
        float maxHullValue,
        float regenAmountInSeconds
) {}