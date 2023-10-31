package net.bfsr.server.dto.converter;

import net.bfsr.entity.ship.module.Modules;
import net.bfsr.server.dto.ModulesModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = WeaponConverter.class)
public interface ModulesConverter {
    @Mapping(target = "weapons", source = "weaponSlots")
    ModulesModel to(Modules modules);

    @Mapping(target = "weaponSlots", source = "weapons")
    Modules from(ModulesModel modulesModel);
}