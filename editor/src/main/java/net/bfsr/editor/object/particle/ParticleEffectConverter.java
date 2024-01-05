package net.bfsr.editor.object.particle;

import net.bfsr.client.config.particle.ParticleEffectConfig;
import net.bfsr.editor.object.EditorObjectConverter;
import net.bfsr.editor.object.SoundConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = SoundConverter.class)
public interface ParticleEffectConverter extends EditorObjectConverter<ParticleEffectConfig, ParticleEffectProperties> {
    @Mapping(target = "isAlphaFromZero", source = "alphaFromZero")
    @Override
    ParticleEffectConfig from(ParticleEffectProperties properties);
    @Mapping(target = "isAlphaFromZero", source = "alphaFromZero")
    @Override
    ParticleEffectProperties to(ParticleEffectConfig config);
}