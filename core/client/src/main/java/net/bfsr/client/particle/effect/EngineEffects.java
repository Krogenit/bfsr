package net.bfsr.client.particle.effect;

import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.SpawnAccumulator;

public final class EngineEffects {
    private static final ParticleEffect secondaryEngine = ParticleEffectsRegistry.INSTANCE.getEffectByPath("engine/secondary_engine");
    private static final ParticleEffect smallEngine = ParticleEffectsRegistry.INSTANCE.getEffectByPath("engine/small");
    private static final ParticleEffect smallEngineNoSmoke = ParticleEffectsRegistry.INSTANCE.getEffectByPath("engine/small_no_smoke");

    public static void secondaryEngine(float x, float y, SpawnAccumulator spawnAccumulator) {
        secondaryEngine.emit(x, y, spawnAccumulator);
    }

    public static void smallEngine(float x, float y, float angle, float size, float velocityX, float velocityY, float r, float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallEngine.emit(x, y, size, size, angle, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }

    public static void smallEngineNoSmoke(float x, float y, float angle, float size, float velocityX, float velocityY, float r, float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallEngineNoSmoke.emit(x, y, size, angle, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }
}