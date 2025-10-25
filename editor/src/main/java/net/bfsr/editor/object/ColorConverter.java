package net.bfsr.editor.object;

import net.bfsr.editor.property.holder.ColorPropertiesHolder;
import net.bfsr.engine.config.ColorConfigurable;
import org.mapstruct.Mapper;

@Mapper
public interface ColorConverter {
    ColorConfigurable from(ColorPropertiesHolder color);
    ColorPropertiesHolder to(ColorConfigurable color);
}