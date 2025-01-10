package net.bfsr.editor.property.converter;

import net.bfsr.config.PolygonConfigurable;
import net.bfsr.editor.property.holder.PolygonPropertiesHolder;
import org.mapstruct.Mapper;

@Mapper
public interface PolygonConverter {
    PolygonConfigurable from(PolygonPropertiesHolder properties);
    PolygonPropertiesHolder to(PolygonConfigurable config);
}