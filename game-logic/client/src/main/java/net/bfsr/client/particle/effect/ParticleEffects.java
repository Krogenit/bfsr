package net.bfsr.client.particle.effect;

import lombok.Getter;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.entity.ParticleManager;

@Getter
public class ParticleEffects {
    private final WeaponEffects weaponEffects;
    private final EngineEffects engineEffects;
    private final GarbageSpawner garbageSpawner;
    private final SmokeEffects smokeEffects;
    private final FireEffects fireEffects;
    private final ExplosionEffects explosionEffects;
    private final JumpEffects jumpEffects;
    private final ShieldEffects shieldEffects;
    private final BeamEffects beamEffects;

    public ParticleEffects(ParticleManager particleManager, ParticleEffectsRegistry effectsRegistry) {
        weaponEffects = new WeaponEffects(effectsRegistry);
        engineEffects = new EngineEffects(effectsRegistry);
        garbageSpawner = new GarbageSpawner(particleManager, effectsRegistry);
        smokeEffects = new SmokeEffects(effectsRegistry);
        fireEffects = new FireEffects(effectsRegistry);
        explosionEffects = new ExplosionEffects(effectsRegistry);
        jumpEffects = new JumpEffects(effectsRegistry);
        shieldEffects = new ShieldEffects(effectsRegistry);
        beamEffects = new BeamEffects(particleManager, effectsRegistry);
    }
}
