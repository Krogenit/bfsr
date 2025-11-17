package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.particle.ParticleRender;
import net.bfsr.engine.world.entity.Particle;
import net.bfsr.engine.world.entity.SpawnAccumulator;
import org.joml.Vector4f;

import java.util.function.Consumer;

public class BeamEffects {
    private final ParticleEffect smallBeam;

    BeamEffects(ParticleEffectsRegistry effectsRegistry) {
        smallBeam = effectsRegistry.get("weapon/beam/small");
    }

    public void beamDamage(float x, float y, float z, float normalX, float normalY, float size, Vector4f color,
                           SpawnAccumulator spawnAccumulator) {
        float angle = (float) Math.atan2(normalX, -normalY) - MathUtils.HALF_PI;
        beam(x, y, z, size, LUT.sin(angle), LUT.cos(angle), 0, 0, color.x, color.y, color.z, color.w, spawnAccumulator);
    }

    public void beam(float x, float y, float z, float size, float sin, float cos, float velocityX, float velocityY, float r,
                     float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallBeam.emit(x, y, z, size, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }

    public void beam(float x, float y, float z, float localX, float localY, float size, float sin, float cos, float velocityX,
                     float velocityY, float r, float g, float b, float a, SpawnAccumulator spawnAccumulator,
                     Consumer<Particle> updateLogic, Consumer<ParticleRender> lastValuesUpdateConsumer) {
        smallBeam.emit(x, y, localX, localY, z, size, size, sin, cos, velocityX, velocityY, r, g, b, a,
                spawnAccumulator, updateLogic, lastValuesUpdateConsumer);
    }
}