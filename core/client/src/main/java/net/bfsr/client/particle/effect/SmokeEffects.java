package net.bfsr.client.particle.effect;

import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.SpawnAccumulator;

public final class SmokeEffects {
    private static final ParticleEffect damageSmoke = ParticleEffectsRegistry.INSTANCE.getEffectByPath("smoke/damage");

    public static void damageSmoke(float x, float y, float size, SpawnAccumulator spawnAccumulator) {
        damageSmoke.emit(x, y, size, spawnAccumulator);
    }
}