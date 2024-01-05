package net.bfsr.server.dto.converter;

import net.bfsr.entity.ship.module.Modules;
import net.bfsr.server.dto.ModulesModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(uses = WeaponConverter.class)
public interface ModulesConverter {
    @Mapping(target = "weapons", source = "weaponSlots")
    ModulesModel to(Modules modules);

    @Mappings({@Mapping(target = "cargo", ignore = true),
            @Mapping(target = "armor", ignore = true),
            @Mapping(target = "weaponSlots", source = "weapons"),
            @Mapping(target = "crew", ignore = true),
            @Mapping(target = "engines", ignore = true),
            @Mapping(target = "hull", ignore = true),
            @Mapping(target = "reactor", ignore = true),
            @Mapping(target = "shield", ignore = true)
    })
    Modules from(ModulesModel modulesModel);
}