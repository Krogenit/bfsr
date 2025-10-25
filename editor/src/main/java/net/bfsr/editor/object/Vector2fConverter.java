package net.bfsr.editor.object;

import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.config.Vector2fConfigurable;
import org.mapstruct.Mapper;

@Mapper
public interface Vector2fConverter {
    Vector2fConfigurable from(Vector2fPropertiesHolder vector);
    Vector2fPropertiesHolder to(Vector2fConfigurable vector);
}