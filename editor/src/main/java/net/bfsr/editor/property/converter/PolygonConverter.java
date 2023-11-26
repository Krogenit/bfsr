package net.bfsr.editor.property.converter;

import net.bfsr.config.PolygonConfigurable;
import net.bfsr.editor.property.holder.PolygonPropertiesHolder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PolygonConverter {
    PolygonConfigurable from(PolygonPropertiesHolder properties);
    @Mapping(target = "name", ignore = true)
    PolygonPropertiesHolder to(PolygonConfigurable config);
}