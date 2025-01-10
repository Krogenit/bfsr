package net.bfsr.editor.object;

import net.bfsr.config.ColorConfigurable;
import net.bfsr.editor.property.holder.ColorPropertiesHolder;
import org.mapstruct.Mapper;

@Mapper
public interface ColorConverter {
    ColorConfigurable from(ColorPropertiesHolder color);
    ColorPropertiesHolder to(ColorConfigurable color);
}