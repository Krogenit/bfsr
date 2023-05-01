package net.bfsr.config.component.engine;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.util.TimeUtils;

@Getter
public class EngineData extends ConfigData {
    private final float forwardAcceleration, backwardAcceleration, sideAcceleration;
    private final float maxForwardVelocity;
    private final float maneuverability;
    private final float angularVelocity;

    public EngineData(EngineConfig engineConfig, int dataIndex) {
        super(engineConfig.name(), dataIndex);
        this.forwardAcceleration = engineConfig.forwardAcceleration();
        this.backwardAcceleration = engineConfig.backwardAcceleration();
        this.sideAcceleration = engineConfig.sideAcceleration();
        this.maxForwardVelocity = engineConfig.maxForwardVelocity();
        this.maneuverability = engineConfig.maneuverability();
        this.angularVelocity = engineConfig.angularVelocity() * TimeUtils.UPDATE_DELTA_TIME;
    }
}