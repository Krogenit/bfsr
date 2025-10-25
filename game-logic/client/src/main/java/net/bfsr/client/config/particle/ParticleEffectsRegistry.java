package net.bfsr.client.config.particle;

import net.bfsr.engine.config.ConfigToDataConverter;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.engine.world.entity.ParticleManager;

import java.util.List;

public class ParticleEffectsRegistry extends ConfigToDataConverter<ParticleEffectConfig, ParticleEffect> {
    public ParticleEffectsRegistry(ParticleManager particleManager) {
        super(PathHelper.CLIENT_CONFIG.resolve("particle-effect"), ParticleEffectConfig.class,
                (fileName, particleEffectConfig) -> particleEffectConfig.getFullPath(), (config, fileName, index, registryId) -> {
                    config.processDeprecated();
                    return new ParticleEffect(config, fileName, index, registryId, particleManager);
                });
    }

    @Override
    public void init(int id) {
        super.init(id);

        List<ParticleEffect> particleEffects = getAll();
        findChild(particleEffects);
        for (int i = 0; i < particleEffects.size(); i++) {
            ParticleEffect particleEffect = particleEffects.get(i);
            particleEffect.init();
        }
    }

    private void findChild(List<ParticleEffect> allEffects) {
        for (int i = 0; i < allEffects.size(); i++) {
            ParticleEffect particleEffect = allEffects.get(i);
            for (int i1 = i + 1; i1 < allEffects.size(); i1++) {
                ParticleEffect particleEffect1 = allEffects.get(i1);
                if (particleEffect1.getPath().contains(particleEffect.getPath())) {
                    particleEffect.addChild(particleEffect1);
                } else if (particleEffect.getPath().contains(particleEffect1.getPath())) {
                    particleEffect1.addChild(particleEffect);
                }
            }
        }
    }
}