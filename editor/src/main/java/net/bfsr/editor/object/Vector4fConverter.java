package net.bfsr.editor.object;

import net.bfsr.config.Vector4fConfigurable;
import net.bfsr.editor.property.holder.Vector4fPropertiesHolder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface Vector4fConverter {
    Vector4fConfigurable from(Vector4fPropertiesHolder vector);
    @Mapping(target = "name", ignore = true)
    Vector4fPropertiesHolder to(Vector4fConfigurable vector);
}