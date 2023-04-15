package net.bfsr.client.particle.spawner;

import net.bfsr.client.component.weapon.WeaponSlotBeam;
import net.bfsr.client.particle.ParticleBeamEffect;
import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.ObjectPool;

public final class BeamSpawner {
    public static final ObjectPool<ParticleBeamEffect> PARTICLE_BEAM_EFFECT_POOL = new ObjectPool<>();

    private static final ParticleEffect smallBeam = ParticleEffectsRegistry.INSTANCE.getEffectByPath("beam/small");

    public static void emitBeam(float x, float y, float size, float angle, float velocityX, float velocityY, float r, float g, float b, float a, SpawnAccumulator spawnAccumulator) {
        smallBeam.emit(x, y, size, size, angle, velocityX, velocityY, r, g, b, a, spawnAccumulator);
    }

    public static ParticleBeamEffect spawnBeamEffect(WeaponSlotBeam slot) {
        return PARTICLE_BEAM_EFFECT_POOL.getOrCreate(ParticleBeamEffect::new).init(slot, TextureRegister.particleBeamEffect);
    }
}