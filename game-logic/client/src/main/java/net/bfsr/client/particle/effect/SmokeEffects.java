package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.entity.SpawnAccumulator;

public class SmokeEffects {
    private final ParticleEffect damageSmoke;

    SmokeEffects(ParticleEffectsRegistry effectsRegistry) {
        damageSmoke = effectsRegistry.get("smoke/damage");
    }

    public void damageSmoke(float x, float y, float size, SpawnAccumulator spawnAccumulator) {
        damageSmoke.emit(x, y, size, spawnAccumulator);
    }
}