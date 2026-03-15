package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.world.entity.Particle;

import java.util.function.Consumer;

public class WeaponEffects {
    private final ParticleEffect smallWeapon;
    private final ParticleEffect bulletHit;

    WeaponEffects(ParticleEffectsRegistry effectsRegistry) {
        smallWeapon = effectsRegistry.get("weapon/small");
        bulletHit = effectsRegistry.get("weapon/bullet_hit");
    }

    public void spawnWeaponShoot(float worldX, float worldY, float localX, float localY, float z, float sin, float cos, float size,
                                 float r, float g, float b, float a, Consumer<Particle> updateLogic) {
        smallWeapon.play(worldX, worldY, localX, localY, z, size, size, sin, cos, 0, 0, r, g, b, a, updateLogic);
    }

    public void spawnDirectedSpark(float contactX, float contactY, float z, float normalX, float normalY, float size, float r,
                                   float g, float b, float a) {
        float angle = (float) Math.atan2(normalX, -normalY) - MathUtils.HALF_PI;
        bulletHit.playSinCos(contactX, contactY, z, size, LUT.sin(angle), LUT.cos(angle), r, g, b, a);
    }
}