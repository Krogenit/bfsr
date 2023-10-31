package net.bfsr.entity.ship.module.engine;

import lombok.Getter;
import net.bfsr.config.component.engine.EngineData;
import net.bfsr.entity.ship.module.Module;
import net.bfsr.entity.ship.module.ModuleType;

@Getter
public class Engine extends Module {
    private final float forwardAcceleration, backwardAcceleration, sideAcceleration;
    private final float maxForwardVelocity;
    private final float maneuverability;
    private final float angularVelocity;

    public Engine(EngineData engineData) {
        this.forwardAcceleration = engineData.getForwardAcceleration();
        this.backwardAcceleration = engineData.getBackwardAcceleration();
        this.sideAcceleration = engineData.getSideAcceleration();
        this.maxForwardVelocity = engineData.getMaxForwardVelocity();
        this.maneuverability = engineData.getManeuverability();
        this.angularVelocity = engineData.getAngularVelocity();
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ENGINE;
    }
}