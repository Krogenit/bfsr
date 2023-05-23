package net.bfsr.client.particle.effect;

import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.config.ParticleEffect;
import net.bfsr.client.particle.config.ParticleEffectsRegistry;

public final class FireEffects {
    private static final ParticleEffect smallFire = ParticleEffectsRegistry.INSTANCE.get("fire/small");

    public static void emitFire(float x, float y, SpawnAccumulator spawnAccumulator) {
        smallFire.emit(x, y, spawnAccumulator);
    }
}