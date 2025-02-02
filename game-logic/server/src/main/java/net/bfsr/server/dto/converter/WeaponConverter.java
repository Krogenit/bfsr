package net.bfsr.server.dto.converter;

import net.bfsr.config.component.weapon.beam.BeamData;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.dto.WeaponModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;

@Mapper
public interface WeaponConverter {
    @Mapping(target = "name", expression = "java(weaponSlot.getGunData().getFileName())")
    WeaponModel to(WeaponSlot weaponSlot);

    @Mapping(target = "hp", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ship", ignore = true)
    @Mapping(target = "localPosition", ignore = true)
    WeaponSlot from(WeaponModel weaponModel);

    @ObjectFactory
    default <T extends WeaponSlot> T to(WeaponModel weaponModel, @TargetType Class<T> entityClass) {
        GunData gunData = ServerGameLogic.getInstance().getConfigConverterManager().getConverter(GunRegistry.class).get(weaponModel.name());
        if (gunData != null) {
            return (T) new WeaponSlot(gunData);
        }

        BeamData beamData = ServerGameLogic.getInstance().getConfigConverterManager().getConverter(BeamRegistry.class)
                .get(weaponModel.name());
        if (beamData != null) {
            return (T) new WeaponSlotBeam(beamData);
        }

        return null;
    }
}