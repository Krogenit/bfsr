package net.bfsr.config.component.weapon.beam;

import lombok.Getter;
import net.bfsr.config.component.weapon.gun.GunConfig;
import net.bfsr.engine.config.Configurable;

@Getter
@Configurable
final class BeamConfig extends GunConfig {
    private float beamMaxRange;
    private float aliveTimeInSeconds;
}