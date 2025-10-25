package net.bfsr.editor.object;

import net.bfsr.editor.sound.SoundProperties;
import net.bfsr.engine.config.ConfigurableSound;
import org.mapstruct.Mapper;

@Mapper
public interface SoundConverter {
    ConfigurableSound from(SoundProperties sound);
    SoundProperties to(ConfigurableSound sound);
}