package net.bfsr.editor.particle;

import net.bfsr.client.particle.config.ParticleEffectConfig;
import org.mapstruct.Mapper;

@Mapper
public interface ParticleEffectConverter {
    ParticleEffectConfig from(ParticleEffectProperties properties);

    ParticleEffectProperties to(ParticleEffectConfig config);
}