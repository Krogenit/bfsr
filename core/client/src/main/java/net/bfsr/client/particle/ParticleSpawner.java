package net.bfsr.client.particle;

import net.bfsr.client.component.weapon.WeaponSlotBeam;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.entity.wreck.ShipWreck;
import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.ObjectPool;
import net.bfsr.util.RandomHelper;
import org.dyn4j.collision.narrowphase.Raycast;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;
import java.util.function.Supplier;

public final class ParticleSpawner {
    private static final Random rand = new Random();
    public static final ObjectPool<Particle> PARTICLE_POOL = new ObjectPool<>();
    public static final ObjectPool<ParticleBeamEffect> PARTICLE_BEAM_EFFECT_POOL = new ObjectPool<>();
    public static final ObjectPool<Wreck> PARTICLE_WREAK_POOL = new ObjectPool<>();
    public static final ObjectPool<ShipWreck> PARTICLE_SHIP_WREAK_POOL = new ObjectPool<>();
    public static final Vector2f CACHED_VECTOR = new Vector2f();
    public static final Supplier<Particle> PARTICLE_SUPPLIER = Particle::new;

    private static final ParticleEffect shipDestroySmall = ParticleEffectsRegistry.INSTANCE.getEffectByPath("explosion/ship_small");
    private static final ParticleEffect smallExplosion = ParticleEffectsRegistry.INSTANCE.getEffectByPath("explosion/small");

    public static void spawnDestroyShipSmall(Ship ship) {
        Vector2f scale = ship.getScale();
        Vector2f pos = ship.getPosition();
        Vector2f velocity = ship.getVelocity();
        shipDestroySmall.play(pos.x, pos.y, scale.x, scale.y, velocity.x, velocity.y);
    }

    public static void spawnLight(float x, float y, float size, float sizeSpeed, float r, float g, float b, float a, float alphaSpeed, boolean alphaFromZero, RenderLayer renderLayer) {
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleLight, x, y, 0, 0, 0, 0, size, size, sizeSpeed, r, g, b, a, alphaSpeed, alphaFromZero, renderLayer);
    }

    public static void spawnSmallExplosion(float x, float y, float size) {
        smallExplosion.play(x, y, size, size);
    }

    public static void spawnExplosion(float x, float y, float size) {
        int count = rand.nextInt(2) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.01f + rand.nextFloat() / 4.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.004f, 0.004f) * 60.0f;
            float sizeVel = 5.4f;
            float alphaVel = 0.84f;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleExplosion, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size, size, sizeVel,
                    1.0f, 1.0f, 1.0f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ADDITIVE);
        }
    }

    public static void spawnBeam(float x, float y, float rotation, float size, float r, float g, float b, float a) {
        float alphaSpeed = 6.0f;
        float sizeSpeed = 30.0f;
        RotationHelper.angleToVelocity(rotation, 10.0f, CACHED_VECTOR);
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleBeamDamage, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, rotation, 0, size, size, sizeSpeed, r, g, b, a,
                alphaSpeed, false, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static ParticleBeamEffect spawnBeamEffect(WeaponSlotBeam slot) {
        return PARTICLE_BEAM_EFFECT_POOL.getOrCreate(ParticleBeamEffect::new).init(slot, TextureRegister.particleBeamEffect);
    }

    public static void spawnLightingIon(Vector2f pos, float size) {
        int count = rand.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0F;
            float sizeVel = 3.2F;
            float alphaVel = 8.0F;
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), size / 4.0f, CACHED_VECTOR);
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleLighting, pos.x + CACHED_VECTOR.x, pos.y + CACHED_VECTOR.y, 0, 0, angle, angleVel, size, size, sizeVel,
                    0.75F, 0.75F, 1, 1.5f, alphaVel, true, RenderLayer.DEFAULT_ADDITIVE);
        }
    }

    public static void spawnRocketShoot(float x, float y, float size) {
        for (int a = 0; a < 2; a++) {
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0F;
            float sizeVel = 9.0f;
            float alphaVel = 1.5f;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleRocketEffect, x, y, 0, 0, angle, angleVel, size, size, sizeVel, 1.0f, 1.0f, 0.5f, 1.0f,
                    alphaVel, true, RenderLayer.DEFAULT_ADDITIVE);
        }

        for (int a = 0; a < 1; a++) {
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0F;
            float sizeVel = 9.0f;
            float alphaVel = 1.2f;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleRocketSmoke, x, y, 0, 0, angle, angleVel, size, size, sizeVel, 0.3f, 0.3f, 0.3f, 1.0f,
                    alphaVel, true, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnSpark(float x, float y, float size, float sizeVel) {
        for (int i = 0; i < 3; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0;
            float alphaVel = 0.03f * 60.0f;

            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleExplosion, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size / 2.0f, size / 2.0f,
                    sizeVel, 1.0f, 0.5f, 0.0f, 1.0f, alphaVel, true, RenderLayer.DEFAULT_ADDITIVE);
        }

        for (int i = 0; i < 2; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0;
            sizeVel = 0;
            float alphaVel = 0.04f * 60.0f;

            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.values()[TextureRegister.particleSpark0.ordinal() + rand.nextInt(4)], x, y, CACHED_VECTOR.x, CACHED_VECTOR.y,
                    angle, angleVel, size, size, sizeVel, 1.0f, 0.5f, 0.0f, 1.0f, alphaVel, true, RenderLayer.DEFAULT_ADDITIVE);
        }
    }

    public static void spawnSmallGarbage(int count, float x, float y, float velocityScale, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f + rand.nextFloat() / 3.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.001f, 0.001f) * 60.0f;
            float sizeVel = 0.025f + rand.nextFloat() / 6.0f * 6.0f;
            float alphaVel = 0.004F * 60.0f;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleGarbage0, x, y, CACHED_VECTOR.x * velocityScale, CACHED_VECTOR.y * velocityScale,
                    angle, angleVel, 1.0f + size, 1.0f + size, sizeVel, 0.6f, 0.6f, 0.6f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnSmallGarbage(int count, float x, float y, float velocityX, float velocityY, float size, float sizeVel, float alphaVel) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.001f, 0.001f) * 60.0f;
            float color = 0.7f;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleGarbage0, x, y, velocityX, velocityY, angle, angleVel, size, size, sizeVel,
                    color, color, color, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnSmallGarbage(int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.02f + rand.nextFloat() * 2.0f, CACHED_VECTOR);
            CACHED_VECTOR.add(velocityX * 0.6f, velocityY * 0.6f);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.060f, 0.060f);
            float sizeVel = 0.025f + rand.nextFloat();
            float alphaVel = 0.12f;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleGarbage0, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, 1.0f + size, 1.0f + size, sizeVel,
                    0.6f, 0.6f, 0.6f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnShipOst(int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f + rand.nextFloat() * 2.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -3.0f, 3.0f);
            float size1 = (2.0f + rand.nextFloat()) * size;
            float sizeVel = 0;
            float alphaVel = 0.06f;
            TextureRegister texture = rand.nextInt(2) == 0 ? TextureRegister.particleShipOst0 : TextureRegister.particleShipOst1;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(texture, x, y, velocityX * 12.0f + CACHED_VECTOR.x, velocityY * 12.0f + CACHED_VECTOR.y, angle, angleVel,
                    size1, size1, sizeVel, 0.5f, 0.5f, 0.5f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnWeaponShoot(TextureRegister texture, Vector2f pos, float angle, float size, float r, float g, float b, float a) {
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleLight, pos.x, pos.y, 0, 0, 0, 0, size, size, 0, r, g, b, a, 0.05f * 60.0f,
                false, RenderLayer.DEFAULT_ADDITIVE);
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(texture, pos.x, pos.y, 0, 0, angle, 0, size, size, 0, r, g, b, a, 0.05f * 60.0f, false,
                RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDamageSmoke(float x, float y, float size, float sizeVel, float velScale) {
        int count = rand.nextInt(4) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.05f + rand.nextFloat() / 3.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0F;
            float alphaVel = 0.015F * 60.0f;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleSmoke1, x, y, CACHED_VECTOR.x * velScale, CACHED_VECTOR.y * velScale, angle, angleVel,
                    size, size, sizeVel, 1.0f, 1.0f, 1.0f, 0.6f, alphaVel, true, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnDamageSmoke(float x, float y, float size) {
        int count = rand.nextInt(4) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.05f + rand.nextFloat() / 3.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0F;
            float sizeVel = 0.75F * 6.0f;
            float alphaVel = 0.015F * 60.0f;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleSmokeRing, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size, size, sizeVel,
                    0.75f, 0.75f, 0.75f, 0.75f, alphaVel, true, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnExplosion(float x, float y, float size, float sizeVel) {
        int count = rand.nextInt(2) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.01f + rand.nextFloat() / 4.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.004f, 0.004f) * 60.0f;
            float alphaVel = 0.014F * 60.0f;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleExplosion, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size, size, sizeVel,
                    1.0f, 1.0f, 1.0f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ADDITIVE);
        }
    }

    public static void spawnShipEngineSmoke(float x, float y) {
        float rot = (rand.nextFloat() * MathUtils.TWO_PI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 100.0f * 60.0f;
        float sizeRandom = RandomHelper.randomFloat(rand, 0.5f, 1.0f);
        float sizeSpeed = 12.0f;
        float alphaSpeed = 2.7f;
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleSmoke2, x, y, 0, 0, rot, rotSpeed, 1.5f * sizeRandom, 1.5f * sizeRandom, sizeSpeed,
                0.5f, 0.5f, 0.5f, 0.75f, alphaSpeed, false, RenderLayer.BACKGROUND_ALPHA_BLENDED);
    }

    public static void spawnBeamDamage(Raycast raycast, float x, float y, float size, float sizeSpeed, Vector4f color) {
        Vector2 normal = raycast.getNormal();
        float rot = (float) Math.atan2(normal.x, -normal.y);
        float alphaSpeed = 6.0f;
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleBeamDamage, x, y, 0, 0, rot, 0, size, size, sizeSpeed,
                color.x, color.y, color.z, color.w, alphaSpeed, false, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDirectedSpark(float contactX, float contactY, float normalX, float normalY, float size, float r, float g, float b, float a) {
        float rot = (float) Math.atan2(normalX, -normalY) - MathUtils.HALF_PI;
        float alphaSpeed = 6.0f;
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleDirectedSpark, contactX, contactY, 0, 0, rot, 0, size, size, 0.0f,
                r, g, b, a, alphaSpeed, false, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDirectedSplat(Contact contact, Vector2 normal, float size, float r, float g, float b, float a) {
        Vector2 point = contact.getPoint();
        float rot = (float) Math.atan2(normal.x, -normal.y);
        rot += MathUtils.HALF_PI;
        float alphaSpeed = 6.0f;
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleDirectedSplat, (float) point.x, (float) point.y, 0, 0, rot, 0, size, size, 0.0f,
                r, g, b, a, alphaSpeed, false, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDisableShield(float x, float y, float size, float sizeSpeed, float r, float g, float b, float a) {
        float rot = (rand.nextFloat() * MathUtils.TWO_PI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 20.0f * 60.0f;
        float alphaSpeed = 0.06f * 60.0f;
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleDisableShield, x, y, 0, 0, rot, rotSpeed, size, size, sizeSpeed,
                r, g, b, a, alphaSpeed, true, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDisableShield(float x, float y, float velocityX, float velocityY, float size, float r, float g, float b, float a) {
        float rot = (rand.nextFloat() * MathUtils.TWO_PI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 20.0f * 60.0f;
        float sizeSpeed = 6.0f;
        float alphaSpeed = 0.06f * 60.0f;
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleDisableShield, x, y, velocityX, velocityY, rot, rotSpeed, size, size, sizeSpeed,
                r, g, b, a, alphaSpeed, true, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDisableShield(float x, float y, float size, float r, float g, float b, float a) {
        float rot = (rand.nextFloat() * MathUtils.TWO_PI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 20.0f * 60.0f;
        float sizeSpeed = 60.0f;
        float alphaSpeed = 0.06f * 60.0f;
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleDisableShield, x, y, 0, 0, rot, rotSpeed, size, size, sizeSpeed,
                r, g, b, a, alphaSpeed, true, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnLight(float x, float y, float velocityX, float velocityY, float size, float r, float g, float b, float a, float alphaSpeed, boolean alphaFromZero,
                                  RenderLayer position) {
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleLight, x, y, velocityX, velocityY, 0, 0, size, size, 0,
                r, g, b, a, alphaSpeed, alphaFromZero, position);
    }

    public static void spawnLight(float x, float y, float size, float r, float g, float b, float a, float alphaSpeed, boolean alphaFromZero, RenderLayer renderLayer) {
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleLight, x, y, 0, 0, 0, 0, size, size, 0, r, g, b, a,
                alphaSpeed, alphaFromZero, renderLayer);
    }

    public static void spawnLight(float x, float y, float size, float r, float g, float b, float a, RenderLayer renderLayer) {
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleLight, x, y, 0, 0, 0, 0, size, size, 0, r, g, b, a, 0.5f * 60.0f, false, renderLayer);
    }

    public static void spawnEngineBack(float x, float y, float velocityX, float velocityY, float rot, float size, float alphaVel, float r, float g, float b, float a, boolean spawnSmoke) {
        float angleVel = 0;
        float sizeVel = -0.9f;
        PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleShipEngineBack, x, y, velocityX * 30.0f, velocityY * 30.0f, rot, angleVel, size, size, sizeVel,
                r, g, b, a, alphaVel, false, RenderLayer.BACKGROUND_ADDITIVE);

        if (spawnSmoke) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.2f, CACHED_VECTOR);
            alphaVel = 0.05f;
            rot = (rand.nextFloat() * MathUtils.TWO_PI);
            sizeVel = 7.0F;
            PARTICLE_POOL.getOrCreate(PARTICLE_SUPPLIER).init(TextureRegister.particleSmoke2, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, rot, angleVel, 3.0f, 3.0f, sizeVel,
                    r, g, b, 0.075f, alphaVel, false, RenderLayer.BACKGROUND_ALPHA_BLENDED);
        }
    }
}