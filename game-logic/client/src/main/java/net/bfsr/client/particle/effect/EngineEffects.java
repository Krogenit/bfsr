package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.world.entity.SpawnAccumulator;

public class EngineEffects {
    private final ParticleEffect secondaryEngine;
    private final ParticleEffect smallEngine;
    private final ParticleEffect smallEngineNoSmoke;

    EngineEffects(ParticleEffectsRegistry particleEffectsRegistry) {
        secondaryEngine = particleEffectsRegistry.get("engine/secondary_engine");
        smallEngine = particleEffectsRegistry.get("engine/small");
        smallEngineNoSmoke = particleEffectsRegistry.get("engine/small_no_smoke");
    }

    public void secondaryEngine(float x, float y, SpawnAccumulator spawnAccumulator) {
        secondaryEngine.emit(x, y, spawnAccumulator);
    }

    public void smallEngine(float x, float y, float sin, float cos, float size, float velocityX, float velocityY, float r,
                            float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallEngine.emit(x, y, size, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }

    public void smallEngineNoSmoke(float x, float y, float sin, float cos, float size, float velocityX, float velocityY,
                                   float r, float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallEngineNoSmoke.emit(x, y, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }
}