package net.bfsr.editor.gui.ship;

import net.bfsr.config.component.ModulesPolygonsConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ModulesConverter {
    ModulesPolygonsConfig from(ModulesPolygonsPropertiesHolder properties);
    @Mapping(target = "name", ignore = true)
    ModulesPolygonsPropertiesHolder to(ModulesPolygonsConfig config);
}