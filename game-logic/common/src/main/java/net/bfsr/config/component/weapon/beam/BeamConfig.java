package net.bfsr.config.component.weapon.beam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.config.component.weapon.gun.GunConfig;
import net.bfsr.engine.config.Configurable;

@Setter
@Getter
@Configurable
@NoArgsConstructor
@AllArgsConstructor
public class BeamConfig extends GunConfig {
    private float beamMaxRange;
    private float aliveTimeInSeconds;
}