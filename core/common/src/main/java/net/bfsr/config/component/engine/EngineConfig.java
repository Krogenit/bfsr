package net.bfsr.config.component.engine;

import net.bfsr.config.Configurable;

@Configurable
public record EngineConfig(
        String name,
        float forwardAcceleration,
        float backwardAcceleration,
        float sideAcceleration,
        float maxForwardVelocity,
        float maneuverability,
        float angularVelocity
) {}