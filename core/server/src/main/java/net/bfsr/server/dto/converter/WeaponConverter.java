package net.bfsr.server.dto.converter;

import net.bfsr.config.weapon.beam.BeamData;
import net.bfsr.config.weapon.beam.BeamRegistry;
import net.bfsr.config.weapon.gun.GunData;
import net.bfsr.config.weapon.gun.GunRegistry;
import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.component.weapon.WeaponSlotBeam;
import net.bfsr.server.dto.WeaponModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;

@Mapper
public interface WeaponConverter {
    @Mapping(target = "name", expression = "java(weaponSlot.getData().getName())")
    WeaponModel to(WeaponSlot weaponSlot);

    @Mapping(target = "rotation", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ship", ignore = true)
    @Mapping(target = "localPosition", ignore = true)
    WeaponSlot from(WeaponModel weaponModel);

    @ObjectFactory
    default <T extends WeaponSlot> T to(WeaponModel weaponModel, @TargetType Class<T> entityClass) {
        GunData gunData = GunRegistry.INSTANCE.get(weaponModel.name());
        if (gunData != null) {
            return (T) new WeaponSlot(gunData);
        }

        BeamData beamData = BeamRegistry.INSTANCE.get(weaponModel.name());
        if (beamData != null) {
            return (T) new WeaponSlotBeam(beamData);
        }

        return null;
    }
}