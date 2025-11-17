package net.bfsr.editor.object.wreck;

import net.bfsr.config.entity.wreck.WreckConfig;
import net.bfsr.editor.object.EditorObjectConverter;
import net.bfsr.editor.object.Vector2fConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = Vector2fConverter.class)
public interface WreckConverter extends EditorObjectConverter<WreckConfig, WreckProperties> {
    @Mapping(target = "name", ignore = true)
    @Override
    WreckProperties to(WreckConfig config);
}
