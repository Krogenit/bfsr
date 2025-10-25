package net.bfsr.editor.object;

import net.bfsr.editor.property.holder.Vector4fPropertiesHolder;
import net.bfsr.engine.config.Vector4fConfigurable;
import org.mapstruct.Mapper;

@Mapper
public interface Vector4fConverter {
    Vector4fConfigurable from(Vector4fPropertiesHolder vector);
    Vector4fPropertiesHolder to(Vector4fConfigurable vector);
}