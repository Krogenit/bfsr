package net.bfsr.editor.object.ship;

import net.bfsr.config.entity.ship.ShipConfig;
import net.bfsr.editor.gui.ship.ModulesConverter;
import net.bfsr.editor.object.ColorConverter;
import net.bfsr.editor.object.EditorObjectConverter;
import net.bfsr.editor.object.Vector2fConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {Vector2fConverter.class, ColorConverter.class, ModulesConverter.class})
public interface ShipConverter extends EditorObjectConverter<ShipConfig, ShipProperties> {
    @Mapping(target = "name", ignore = true)
    @Override
    ShipProperties to(ShipConfig config);
}