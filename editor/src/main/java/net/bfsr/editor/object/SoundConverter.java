package net.bfsr.editor.object;

import net.bfsr.config.ConfigurableSound;
import net.bfsr.editor.sound.SoundProperties;
import org.mapstruct.Mapper;

@Mapper
public interface SoundConverter {
    ConfigurableSound from(SoundProperties sound);
    SoundProperties to(ConfigurableSound sound);
}