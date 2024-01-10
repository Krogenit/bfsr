package net.bfsr.config.component.weapon.beam;

import lombok.Getter;
import net.bfsr.config.Configurable;
import net.bfsr.config.component.weapon.gun.GunConfig;

@Getter
@Configurable
final class BeamConfig extends GunConfig {
    private float beamMaxRange;
    private float aliveTimeInSeconds;
}