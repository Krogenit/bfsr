package net.bfsr.client.particle.effect;

import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;

public final class WeaponEffects {
    private static final ParticleEffect smallWeapon = ParticleEffectsRegistry.INSTANCE.get("weapon/small");
    private static final ParticleEffect bulletHit = ParticleEffectsRegistry.INSTANCE.get("weapon/bullet_hit");
    private static final ParticleEffect lightingIon = ParticleEffectsRegistry.INSTANCE.get("weapon/lighting_ion");

    public static void spawnWeaponShoot(Vector2f pos, float sin, float cos, float size, Vector2f velocity, float r, float g, float b, float a) {
        smallWeapon.play(pos.x, pos.y, size, size, sin, cos, velocity.x, velocity.y, r, g, b, a);
    }

    public static void spawnDirectedSpark(float contactX, float contactY, float normalX, float normalY, float size, float r, float g, float b, float a) {
        float angle = (float) Math.atan2(normalX, -normalY) - MathUtils.HALF_PI;
        bulletHit.playSinCos(contactX, contactY, size, LUT.sin(angle), LUT.cos(angle), r, g, b, a);
    }

    public static void lightingIon(Vector2f pos, float size) {
        int count = ParticleManager.RAND.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * ParticleManager.RAND.nextFloat(), size / 4.0f, ParticleManager.CACHED_VECTOR);
            lightingIon.play(pos.x + ParticleManager.CACHED_VECTOR.x, pos.y + ParticleManager.CACHED_VECTOR.y, size, size);
        }
    }
}