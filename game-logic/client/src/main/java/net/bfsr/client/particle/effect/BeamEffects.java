package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.ParticleBeamEffect;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import org.joml.Vector4f;

public final class BeamEffects {
    public static final ObjectPool<ParticleBeamEffect> PARTICLE_BEAM_EFFECT_POOL = new ObjectPool<>(ParticleBeamEffect::new);

    private static final ParticleEffect smallBeam = ParticleEffectsRegistry.INSTANCE.get("weapon/beam/small");

    public static void beamDamage(float x, float y, float normalX, float normalY, float size, Vector4f color,
                                  SpawnAccumulator spawnAccumulator) {
        float angle = (float) Math.atan2(normalX, -normalY) - MathUtils.HALF_PI;
        beam(x, y, size, LUT.sin(angle), LUT.cos(angle), 0, 0, color.x, color.y, color.z, color.w, spawnAccumulator);
    }

    public static void beam(float x, float y, float size, float sin, float cos, float velocityX, float velocityY, float r,
                            float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallBeam.emit(x, y, size, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }

    public static ParticleBeamEffect beamEffect(WeaponSlotBeam slot, Vector4f color) {
        return PARTICLE_BEAM_EFFECT_POOL.get().init(slot, TextureRegister.particleBeamEffect, color);
    }
}