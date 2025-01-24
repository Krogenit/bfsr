package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.entity.SpawnAccumulator;

public class FireEffects {
    private final ParticleEffect smallFire;

    FireEffects(ParticleEffectsRegistry effectsRegistry) {
        smallFire = effectsRegistry.get("fire/small");
    }

    public void emitFire(float x, float y, SpawnAccumulator spawnAccumulator) {
        smallFire.emit(x, y, spawnAccumulator);
    }
}