package net.bfsr.client.particle.effect;

import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.RandomHelper;
import org.joml.Vector2f;

import java.util.function.Supplier;

import static net.bfsr.client.particle.effect.ParticleSpawner.CACHED_VECTOR;
import static net.bfsr.client.particle.effect.ParticleSpawner.RAND;

public final class GarbageSpawner {
    private static final ParticleEffect ost = ParticleEffectsRegistry.INSTANCE.getEffectByPath("garbage/ost");

    public static void bulletHullDamage(float x, float y, float velocityX, float velocityY, float normalX, float normalY, Supplier<Boolean> shouldSpawnOstSupplier) {
        if (shouldSpawnOstSupplier.get() && RAND.nextInt(2) == 0) {
            spawnShipOst(x, y, velocityX + normalX * (RAND.nextFloat() * 0.5f + 0.5f), velocityY + normalY * (RAND.nextFloat() * 0.5f + 0.5f));
        }
        smallGarbage(1 + RAND.nextInt(3), x, y, velocityX + normalX, velocityY + normalY, 1.1f * (RAND.nextFloat() + 0.5f), 3.0f, 0.5f);
    }

    public static void beamHullDamage(float x, float y, float velocityX, float velocityY) {
        beamHullDamage(x, y, velocityX, velocityY, () -> true);
    }

    public static void beamHullDamage(float x, float y, float velocityX, float velocityY, Supplier<Boolean> shouldSpawnOstSupplier) {
        if (RAND.nextInt(5) == 0) {
            if (shouldSpawnOstSupplier.get() && RAND.nextInt(50) == 0) {
                RotationHelper.angleToVelocity(MathUtils.TWO_PI * RAND.nextFloat(), 1.5f, CACHED_VECTOR);
                spawnShipOst(x, y, velocityX + CACHED_VECTOR.x, velocityY + CACHED_VECTOR.y);
            }
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * RAND.nextFloat(), 1.5f, CACHED_VECTOR);
            smallGarbage(RAND.nextInt(4), x, y, velocityX + CACHED_VECTOR.x, velocityY + CACHED_VECTOR.y, 2.0f * RAND.nextFloat());
        }
    }

    public static void spawnShipOst(float x, float y, float velocityX, float velocityY) {
        RotationHelper.angleToVelocity(MathUtils.TWO_PI * RAND.nextFloat(), 0.2f + RAND.nextFloat() * 2.0f, CACHED_VECTOR);
        ost.play(x, y, 0, 0, velocityX + CACHED_VECTOR.x, velocityY + CACHED_VECTOR.y);
    }

    public static void smallGarbage(int count, float x, float y, float velocityX, float velocityY, float size, float sizeVel, float alphaVel) {
        smallGarbage(count, x, y, velocityX, velocityY, () -> CACHED_VECTOR.set(0), size, sizeVel, alphaVel);
    }

    public static void smallGarbage(int count, float x, float y, float velocityX, float velocityY, float size) {
        smallGarbage(count, x, y, velocityX, velocityY, () -> {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * RAND.nextFloat(), 0.02f + RAND.nextFloat() * 2.0f, CACHED_VECTOR);
            return CACHED_VECTOR;
        }, size, 0.5f, 0.12f);
    }

    public static void smallGarbage(int count, float x, float y, float velocityX, float velocityY, Supplier<Vector2f> localVelocitySupplier, float size, float sizeVel, float alphaVel) {
        for (int i = 0; i < count; i++) {
            Vector2f localVelocity = localVelocitySupplier.get();
            float angularVelocity = RandomHelper.randomFloat(RAND, -0.06f, 0.06f);
            ParticleSpawner.PARTICLE_POOL.getOrCreate(ParticleSpawner.PARTICLE_SUPPLIER).init(TextureRegister.particleGarbage0, x, y, velocityX + localVelocity.x, velocityY + localVelocity.y,
                    MathUtils.TWO_PI * RAND.nextFloat(), angularVelocity, size, size, sizeVel, 0.6f, 0.6f, 0.6f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }
}