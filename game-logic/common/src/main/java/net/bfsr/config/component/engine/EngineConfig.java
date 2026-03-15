package net.bfsr.config.component.engine;

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
public class EngineConfig {
    private float forwardAcceleration;
    private float backwardAcceleration;
    private float sideAcceleration;
    private float maxForwardVelocity;
    private float maneuverability;
    private float angularVelocity;
}