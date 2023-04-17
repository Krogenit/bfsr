package net.bfsr.client.particle.effect;

import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;

public final class WeaponEffects {
    private static final ParticleEffect smallWeapon = ParticleEffectsRegistry.INSTANCE.getEffectByPath("weapon/small");
    private static final ParticleEffect bulletHit = ParticleEffectsRegistry.INSTANCE.getEffectByPath("weapon/bullet_hit");
    private static final ParticleEffect lightingIon = ParticleEffectsRegistry.INSTANCE.getEffectByPath("weapon/lighting_ion");

    public static void spawnWeaponShoot(Vector2f pos, float angle, float size, float r, float g, float b, float a) {
        smallWeapon.play(pos.x, pos.y, size, size, angle, 0, 0, r, g, b, a);
    }

    public static void spawnDirectedSpark(float contactX, float contactY, float normalX, float normalY, float size, float r, float g, float b, float a) {
        bulletHit.play(contactX, contactY, size, (float) Math.atan2(normalX, -normalY) - MathUtils.HALF_PI, r, g, b, a);
    }

    public static void lightingIon(Vector2f pos, float size) {
        int count = ParticleSpawner.RAND.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * ParticleSpawner.RAND.nextFloat(), size / 4.0f, ParticleSpawner.CACHED_VECTOR);
            lightingIon.play(pos.x + ParticleSpawner.CACHED_VECTOR.x, pos.y + ParticleSpawner.CACHED_VECTOR.y, size, size);
        }
    }
}