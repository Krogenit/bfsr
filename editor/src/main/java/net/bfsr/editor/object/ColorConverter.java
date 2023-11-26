package net.bfsr.editor.object;

import net.bfsr.config.ColorConfigurable;
import net.bfsr.editor.property.holder.ColorPropertiesHolder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ColorConverter {
    ColorConfigurable from(ColorPropertiesHolder color);
    @Mapping(target = "name", ignore = true)
    ColorPropertiesHolder to(ColorConfigurable color);
}