package net.bfsr.editor.object;

import net.bfsr.config.ConfigurableSound;
import net.bfsr.editor.sound.SoundProperties;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface SoundConverter {
    ConfigurableSound from(SoundProperties sound);

    @Mapping(target = "name", ignore = true)
    SoundProperties to(ConfigurableSound sound);
}