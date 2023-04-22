package net.bfsr.config.weapon.beam;

import lombok.Getter;
import net.bfsr.config.weapon.gun.GunConfig;
import net.bfsr.config.weapon.gun.GunData;
import net.bfsr.entity.bullet.BulletDamage;

@Getter
public class BeamData extends GunData {
    private final float beamMaxRange;
    private final BulletDamage damage;

    public BeamData(BeamConfig config, int dataIndex) {
        super(new GunConfig(config.name(), config.reloadTimeInSeconds(), config.energyCost(), null, config.size(), config.texture(), config.color(), config.sounds(),
                config.vertices()), dataIndex);
        this.beamMaxRange = config.beamMaxRange();
        this.damage = new BulletDamage(config.damage().armor(), config.damage().hull(), config.damage().shield());
    }
}