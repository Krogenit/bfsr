package net.bfsr.config.component.engine;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.ConfigData;

@Getter
public class EnginesData extends ConfigData {
    private final float forwardAcceleration, backwardAcceleration, sideAcceleration;
    private final float maxForwardVelocity;
    private final float maneuverability;
    private final float angularVelocity;

    EnginesData(EngineConfig engineConfig, String fileName, int id, int registryId) {
        super(fileName, id, registryId);
        this.forwardAcceleration = engineConfig.forwardAcceleration();
        this.backwardAcceleration = engineConfig.backwardAcceleration();
        this.sideAcceleration = engineConfig.sideAcceleration();
        this.maxForwardVelocity = engineConfig.maxForwardVelocity();
        this.maneuverability = engineConfig.maneuverability();
        this.angularVelocity = Engine.convertToDeltaTime(engineConfig.angularVelocity());
    }
}