package net.bfsr.server.dto.converter;

import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.server.dto.WeaponModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(uses = WeaponFactory.class)
public interface WeaponConverter {
    @Mapping(target = "name", expression = "java(weaponSlot.getData().getFileName())")
    WeaponModel to(WeaponSlot weaponSlot);

    @Mappings({
            @Mapping(target = "hp", ignore = true), @Mapping(target = "id", ignore = true),
            @Mapping(target = "ship", ignore = true), @Mapping(target = "localPosition", ignore = true),
            @Mapping(target = "configData", ignore = true), @Mapping(target = "fixture", ignore = true),
            @Mapping(target = "gunData", ignore = true), @Mapping(target = "moduleEventBus", ignore = true),
            @Mapping(target = "weaponSlotEventBus", ignore = true), @Mapping(target = "data", ignore = true)
    })
    WeaponSlot from(WeaponModel weaponModel);
}