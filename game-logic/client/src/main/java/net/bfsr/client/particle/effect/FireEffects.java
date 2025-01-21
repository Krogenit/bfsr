package net.bfsr.client.particle.effect;

import net.bfsr.client.Client;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.particle.SpawnAccumulator;

public final class FireEffects {
    private static final ParticleEffect smallFire = Client.get().getParticleEffect("fire/small");

    public static void emitFire(float x, float y, SpawnAccumulator spawnAccumulator) {
        smallFire.emit(x, y, spawnAccumulator);
    }
}