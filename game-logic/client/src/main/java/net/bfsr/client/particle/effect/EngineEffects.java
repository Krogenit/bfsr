package net.bfsr.client.particle.effect;

import net.bfsr.client.Client;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.particle.SpawnAccumulator;

public final class EngineEffects {
    private static final ParticleEffect secondaryEngine = Client.get().getParticleEffect("engine/secondary_engine");
    private static final ParticleEffect smallEngine = Client.get().getParticleEffect("engine/small");
    private static final ParticleEffect smallEngineNoSmoke = Client.get().getParticleEffect("engine/small_no_smoke");

    public static void secondaryEngine(float x, float y, SpawnAccumulator spawnAccumulator) {
        secondaryEngine.emit(x, y, spawnAccumulator);
    }

    public static void smallEngine(float x, float y, float sin, float cos, float size, float velocityX, float velocityY, float r,
                                   float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallEngine.emit(x, y, size, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }

    public static void smallEngineNoSmoke(float x, float y, float sin, float cos, float size, float velocityX, float velocityY,
                                          float r, float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallEngineNoSmoke.emit(x, y, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }
}