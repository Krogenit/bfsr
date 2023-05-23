package net.bfsr.client.particle.config;

import lombok.Getter;
import net.bfsr.config.ConfigToDataConverter;

import java.util.ArrayList;
import java.util.List;

public class ParticleEffectsRegistry extends ConfigToDataConverter<ParticleEffectConfig, ParticleEffect> {
    public static final ParticleEffectsRegistry INSTANCE = new ParticleEffectsRegistry();

    @Getter
    private final List<ParticleEffectConfig> allConfigs = new ArrayList<>();

    public ParticleEffectsRegistry() {
        super("particle-effect", ParticleEffectConfig.class, ParticleEffectConfig::getPath, (config, dataIndex) -> {
            config.processDeprecated();
            return new ParticleEffect(config, dataIndex);
        });
    }

    @Override
    public void init() {
        super.init();

        List<ParticleEffect> particleEffects = getAll();
        findChild(particleEffects);
        for (int i = 0; i < particleEffects.size(); i++) {
            ParticleEffect particleEffect = particleEffects.get(i);
            particleEffect.init();
        }
    }

    @Override
    public void add(ParticleEffectConfig config) {
        super.add(config);
        allConfigs.add(config);
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

    @Override
    public void remove(String key) {
        super.remove(key);
        allConfigs.removeIf(particleEffectConfig -> particleEffectConfig.getPath().equals(key));
    }
}