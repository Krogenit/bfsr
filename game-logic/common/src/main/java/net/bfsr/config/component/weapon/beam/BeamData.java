package net.bfsr.config.component.weapon.beam;

import lombok.Getter;
import net.bfsr.config.component.weapon.gun.GunData;

@Getter
public class BeamData extends GunData {
    private final float beamMaxRange;

    BeamData(BeamConfig config, String fileName, int id) {
        super(config, fileName, id);
        this.beamMaxRange = config.getBeamMaxRange();
    }
}