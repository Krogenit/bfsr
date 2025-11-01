package net.bfsr.editor.object.station;

import net.bfsr.config.entity.station.StationConfig;
import net.bfsr.editor.object.EditorObjectConverter;
import net.bfsr.editor.object.Vector2fConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {Vector2fConverter.class})
public interface StationConverter extends EditorObjectConverter<StationConfig, StationProperties> {
    @Mapping(target = "name", ignore = true)
    @Override
    StationProperties to(StationConfig config);
}