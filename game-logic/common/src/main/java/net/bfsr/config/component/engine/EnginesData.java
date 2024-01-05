package net.bfsr.config.component.engine;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.engine.util.TimeUtils;

@Getter
public class EnginesData extends ConfigData {
    private final float forwardAcceleration, backwardAcceleration, sideAcceleration;
    private final float maxForwardVelocity;
    private final float maneuverability;
    private final float angularVelocity;

    EnginesData(EngineConfig engineConfig, String fileName, int id) {
        super(fileName, id);
        this.forwardAcceleration = engineConfig.forwardAcceleration();
        this.backwardAcceleration = engineConfig.backwardAcceleration();
        this.sideAcceleration = engineConfig.sideAcceleration();
        this.maxForwardVelocity = engineConfig.maxForwardVelocity();
        this.maneuverability = engineConfig.maneuverability();
        this.angularVelocity = engineConfig.angularVelocity() * TimeUtils.UPDATE_DELTA_TIME;
    }
}