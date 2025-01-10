package net.bfsr.editor.gui.ship;

import net.bfsr.config.component.ModulesPolygonsConfig;
import org.mapstruct.Mapper;

@Mapper
public interface ModulesConverter {
    ModulesPolygonsConfig from(ModulesPolygonsPropertiesHolder properties);
    ModulesPolygonsPropertiesHolder to(ModulesPolygonsConfig config);
}