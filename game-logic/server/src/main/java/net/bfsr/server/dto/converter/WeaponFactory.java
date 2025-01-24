package net.bfsr.server.dto.converter;

import net.bfsr.config.component.weapon.beam.BeamData;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.dto.WeaponModel;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;

public class WeaponFactory {
    private final GunRegistry gunRegistry = ServerGameLogic.get().getConfigConverterManager().getConverter(GunRegistry.class);
    private final BeamRegistry beamRegistry = ServerGameLogic.get().getConfigConverterManager().getConverter(BeamRegistry.class);

    @ObjectFactory
    public <T extends WeaponSlot> T to(WeaponModel weaponModel, @TargetType Class<T> entityClass) {
        GunData gunData = gunRegistry.get(weaponModel.name());
        if (gunData != null) {
            return (T) new WeaponSlot(gunData);
        }

        BeamData beamData = beamRegistry.get(weaponModel.name());
        if (beamData != null) {
            return (T) new WeaponSlotBeam(beamData);
        }

        return null;
    }
}
