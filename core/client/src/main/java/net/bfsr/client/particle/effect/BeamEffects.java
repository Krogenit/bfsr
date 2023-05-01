package net.bfsr.client.particle.effect;

import net.bfsr.client.particle.ParticleBeamEffect;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.config.ParticleEffect;
import net.bfsr.client.particle.config.ParticleEffectsRegistry;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.math.LUT;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.ObjectPool;
import org.dyn4j.collision.narrowphase.Raycast;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector4f;

public final class BeamEffects {
    public static final ObjectPool<ParticleBeamEffect> PARTICLE_BEAM_EFFECT_POOL = new ObjectPool<>();

    private static final ParticleEffect smallBeam = ParticleEffectsRegistry.INSTANCE.get("weapon/beam/small");

    public static void beamDamage(Raycast raycast, float x, float y, float size, Vector4f color, SpawnAccumulator spawnAccumulator) {
        Vector2 normal = raycast.getNormal();
        float angle = (float) Math.atan2(normal.x, -normal.y);
        beam(x, y, size, LUT.sin(angle), LUT.cos(angle), 0, 0, color.x, color.y, color.z, color.w, spawnAccumulator);
    }

    public static void beam(float x, float y, float size, float sin, float cos, float velocityX, float velocityY, float r, float g, float b, float a,
                            SpawnAccumulator spawnAccumulator) {
        smallBeam.emit(x, y, size, size, sin, cos, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }

    public static ParticleBeamEffect beamEffect(WeaponSlotBeam slot, Vector4f color) {
        return PARTICLE_BEAM_EFFECT_POOL.getOrCreate(ParticleBeamEffect::new).init(slot, TextureRegister.particleBeamEffect, color);
    }
}