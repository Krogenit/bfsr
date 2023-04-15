package net.bfsr.client.particle.spawner;

import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.SpawnAccumulator;

public final class FireSpawner {
    private static final ParticleEffect smallFire = ParticleEffectsRegistry.INSTANCE.getEffectByPath("fire/small");

    public static void emitFire(float x, float y, SpawnAccumulator spawnAccumulator) {
        smallFire.emit(x, y, spawnAccumulator);
    }
}