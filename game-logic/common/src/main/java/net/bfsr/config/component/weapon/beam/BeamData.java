package net.bfsr.config.component.weapon.beam;

import lombok.Getter;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.engine.Engine;

@Getter
public class BeamData extends GunData {
    private final float beamMaxRange;
    private final float aliveTimeInTicks;

    BeamData(BeamConfig config, String fileName, int id) {
        super(config, fileName, id);
        this.beamMaxRange = config.getBeamMaxRange();
        this.aliveTimeInTicks = Engine.convertToTicks(config.getAliveTimeInSeconds());
    }
}