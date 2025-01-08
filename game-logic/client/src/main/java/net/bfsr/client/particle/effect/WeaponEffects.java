package net.bfsr.client.particle.effect;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.Particle;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;

import java.util.function.Consumer;

public final class WeaponEffects {
    private static final XoRoShiRo128PlusRandom RANDOM = new XoRoShiRo128PlusRandom();

    private static final ParticleEffect smallWeapon = ParticleEffectsRegistry.INSTANCE.get("weapon/small");
    private static final ParticleEffect bulletHit = ParticleEffectsRegistry.INSTANCE.get("weapon/bullet_hit");
    private static final ParticleEffect lightingIon = ParticleEffectsRegistry.INSTANCE.get("weapon/lighting_ion");

    public static void spawnWeaponShoot(float worldX, float worldY, float localX, float localY, float sin, float cos, float size,
                                        float r, float g, float b, float a, Consumer<Particle> updateLogic) {
        smallWeapon.play(worldX, worldY, localX, localY, size, size, sin, cos, 0, 0, r, g, b, a, updateLogic);
    }

    public static void spawnDirectedSpark(float contactX, float contactY, float normalX, float normalY, float size, float r,
                                          float g, float b, float a) {
        float angle = (float) Math.atan2(normalX, -normalY) - MathUtils.HALF_PI;
        bulletHit.playSinCos(contactX, contactY, size, LUT.sin(angle), LUT.cos(angle), r, g, b, a);
    }

    public static void lightingIon(Vector2f pos, float size) {
        int count = RANDOM.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * RANDOM.nextFloat(), size / 4.0f,
                    ParticleManager.CACHED_VECTOR);
            lightingIon.play(pos.x + ParticleManager.CACHED_VECTOR.x, pos.y + ParticleManager.CACHED_VECTOR.y, size, size);
        }
    }
}