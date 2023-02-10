package net.bfsr.client.particle;

import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.server.PacketSpawnParticle;
import net.bfsr.server.MainServer;
import net.bfsr.util.ObjectPool;
import net.bfsr.util.RandomHelper;
import net.bfsr.world.World;
import net.bfsr.world.WorldServer;
import org.dyn4j.collision.narrowphase.Raycast;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public final class ParticleSpawner {
    private static final Random rand = new Random();
    public static final ObjectPool<Particle> PARTICLE_POOL = new ObjectPool<>();
    public static final ObjectPool<ParticleBeamEffect> PARTICLE_BEAM_EFFECT_POOL = new ObjectPool<>();
    public static final ObjectPool<Wreck> PARTICLE_WREAK_POOL = new ObjectPool<>();
    public static final Vector2f CACHED_VECTOR = new Vector2f();

    public static void spawnDestroyShipSmall(Ship ship) {
        Vector2f scale = ship.getScale();
        Vector2f pos = ship.getPosition();
        Vector2f velocity = ship.getVelocity();
        float baseSize = 4.0f + scale.x * 0.25f;
        World w = ship.getWorld();
        Random rand = w.getRand();
        if (w.isRemote()) {
            ship.setDead(true);
            Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion1, pos.x, pos.y));
            spawnShockwave(0, pos, baseSize + 3.0f);
            for (int i = 0; i < 8; i++) {
                RotationHelper.angleToVelocity(rand.nextFloat() * MathUtils.TWO_PI, 7.0f, CACHED_VECTOR);
                spawnMediumGarbage(1, pos.x + -scale.x / 2.25f + rand.nextInt((int) (scale.x / 1.25f)), pos.y + -scale.y / 2.25f + rand.nextInt((int) (scale.y / 1.25f)),
                        velocity.x * 0.25f + CACHED_VECTOR.x, velocity.y * 0.25f + CACHED_VECTOR.y, baseSize);
            }
            float size = (scale.x + scale.y) * 1.1f;
            spawnSpark(pos.x, pos.y, size);
            spawnLight(pos.x, pos.y, size, 4.0f * 6.0f, 1, 0.5f, 0.4f, 1.0f, 0.05f * 60.0f, true, RenderLayer.DEFAULT_ADDITIVE);
            spawnRocketShoot(pos.x, pos.y, size);
            spawnSmallGarbage(4 + rand.nextInt(10), pos.x, pos.y, velocity.x * 0.025f, velocity.y * 0.025f, 1.0f);
        } else {
            spawnDamageDebris(w, rand.nextInt(3), pos.x, pos.y, velocity.x * 0.025f, velocity.y * 0.025f, 1.0f);
            spawnDamageWrecks(w, rand.nextInt(2), pos.x, pos.y, velocity.x * 0.25f, velocity.y * 0.25f);
            Vector2 bodyVelocity = ship.getBody().getLinearVelocity();
            float rot = ship.getRotation();
            if (rand.nextInt(2) == 0) {
                spawnShipWreck(ship, 0, pos.x, pos.y, rot, -rot * 3.0f + (float) bodyVelocity.x * 0.4f, -rot * 3.0f + (float) bodyVelocity.y * 0.4f, 0.02f, 750.0f);
            }

            if (rand.nextInt(2) == 0) {
                spawnShipWreck(ship, 1, pos.x, pos.y, rot, rot * 3.0f - (float) bodyVelocity.x * 0.4f, rot * 3.0f - (float) bodyVelocity.y * 0.4f, 0.02f, 750.0f);
            }
        }
    }

    public static void spawnShipWreck(Ship s, int textureOffset, float x, float y, float angle, float velocityX, float velocityY, float alphaVel, float wreckLifeTime) {
        float angleVel = (-0.005f + rand.nextFloat() / 200.0f) * 60.0f;
        Wreck wreck = PARTICLE_WREAK_POOL.getOrCreate(Wreck::new).init(s.getWorld().getNextId(), textureOffset, s, x, y, velocityX, velocityY, angle, angleVel,
                s.getScale().x, s.getScale().y, 0.5f, 0.5f, 0.5f, 1.0f, alphaVel, wreckLifeTime);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnParticle(wreck), x, y, WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public static void spawnDamageDebris(World world, int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(rand.nextFloat() * MathUtils.TWO_PI, 4.0f + rand.nextFloat() * 2.0f, CACHED_VECTOR);
            CACHED_VECTOR.add(velocityX, velocityY).mul(0.7f);
            float angle = rand.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + rand.nextFloat() / 200.0f) * 60.0f;
            float size2 = (1.0F - rand.nextFloat() / 3.0F) * 2.0f * size;
            float alphaVel = 0.1f;//RandomHelper.randomFloat(rand, 0.0003f, 0.0006f) * 60f;
            boolean isFire = rand.nextInt(3) == 0;
            boolean isFireExplosion = isFire && rand.nextInt(5) == 0;
            Wreck wreck = PARTICLE_WREAK_POOL.getOrCreate(Wreck::new).init(world, world.getNextId(), rand.nextInt(6), false, isFire, isFireExplosion, x, y,
                    CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size2, size2, 0.5f, 0.5f, 0.5f, 1.0f, alphaVel);
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnParticle(wreck), x, y, WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public static void spawnDamageWrecks(World world, int count, float x, float y, float velocityX, float velocityY) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(rand.nextFloat() * MathUtils.TWO_PI, 4.0f + rand.nextFloat() * 2.0f, CACHED_VECTOR);
            float angle = rand.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + rand.nextFloat() / 200.0f) * 60.0f;
            float size = (1.0F - rand.nextFloat() / 3.0F) * 4.0f;
            float alphaVel = 0.04f;
            boolean isFireExplosion = rand.nextInt(4) == 0;
            Wreck wreck = PARTICLE_WREAK_POOL.getOrCreate(Wreck::new).init(world, world.getNextId(), rand.nextInt(3), true, true, isFireExplosion,
                    x, y, CACHED_VECTOR.x + velocityX * 0.7f, CACHED_VECTOR.y + velocityY * 0.7f, angle, angleVel, size, size, 0.5f, 0.5f, 0.5f, 1.0f, alphaVel);
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnParticle(wreck), x, y, WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public static void spawnBeam(float x, float y, float rotation, float size, float r, float g, float b, float a) {
        float alphaSpeed = 6.0f;
        float sizeSpeed = 30.0f;
        RotationHelper.angleToVelocity(rotation, 10.0f, CACHED_VECTOR);
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleBeamDamage, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, rotation, 0, size, size, sizeSpeed, r, g, b, a,
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
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleLighting, pos.x + CACHED_VECTOR.x, pos.y + CACHED_VECTOR.y, 0, 0, angle, angleVel, size, size, sizeVel,
                    0.75F, 0.75F, 1, 1.5f, alphaVel, true, RenderLayer.DEFAULT_ADDITIVE);
        }
    }

    public static void spawnRocketShoot(float x, float y, float size) {
        for (int a = 0; a < 2; a++) {
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0F;
            float sizeVel = 1.5F * 6.0f;
            float alphaVel = 0.025F * 60.0f;
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleRocketEffect, x, y, 0, 0, angle, angleVel, size, size, sizeVel, 1.0f, 1.0f, 0.5f, 1.0f,
                    alphaVel, true, RenderLayer.DEFAULT_ADDITIVE);
        }

        for (int a = 0; a < 1; a++) {
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0F;
            float sizeVel = 1.5F * 6.0f;
            float alphaVel = 0.02F * 60.0f;
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleRocketSmoke, x, y, 0, 0, angle, angleVel, size, size, sizeVel, 0.3f, 0.3f, 0.3f, 1.0f,
                    alphaVel, true, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnShockwave(int type, Vector2f pos, float size) {
        float angle = MathUtils.TWO_PI * rand.nextFloat();
        float angleVel = 0.0F;
        float sizeVel = type == 1 ? 0.8F * 6.0f : type == 2 ? 1.5F * 6.0f : 6.0F * 6.0f;
        float alphaVel = type == 1 ? 0.004F * 60.0f : type == 2 ? 0.006F * 60.0f : 0.02F * 60.0f;
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.values()[TextureRegister.particleSockwaveSmall.ordinal() + type], pos.x, pos.y, 0, 0, angle, angleVel, size, size,
                sizeVel, 0.5F, 0.5F, 0.5F, 1, alphaVel, false, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnSpark(float x, float y, float size, float sizeVel) {
        for (int i = 0; i < 3; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0;
            float alphaVel = 0.03f * 60.0f;

            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleExplosion, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size / 2.0f, size / 2.0f,
                    sizeVel, 1.0f, 0.5f, 0.0f, 1.0f, alphaVel, true, RenderLayer.DEFAULT_ADDITIVE);
        }

        for (int i = 0; i < 2; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0;
            sizeVel = 0;
            float alphaVel = 0.04f * 60.0f;

            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.values()[TextureRegister.particleSpark0.ordinal() + rand.nextInt(4)], x, y, CACHED_VECTOR.x, CACHED_VECTOR.y,
                    angle, angleVel, size, size, sizeVel, 1.0f, 0.5f, 0.0f, 1.0f, alphaVel, true, RenderLayer.DEFAULT_ADDITIVE);
        }
    }

    public static void spawnSpark(float x, float y, float size) {
        for (int i = 0; i < 3; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0;
            float sizeVel = 6.0f;
            float alphaVel = 0.03f * 60.0f;

            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleExplosion, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size / 2.0f, size / 2.0f, sizeVel,
                    1.0f, 0.5f, 0.0f, 1.0f, alphaVel, true, RenderLayer.DEFAULT_ADDITIVE);
        }

        for (int i = 0; i < 2; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0;
            float sizeVel = 0;
            float alphaVel = 0.04f * 60.0f;

            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.values()[TextureRegister.particleSpark0.ordinal() + rand.nextInt(4)], x, y, CACHED_VECTOR.x, CACHED_VECTOR.y,
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
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleGarbage0, x, y, CACHED_VECTOR.x * velocityScale, CACHED_VECTOR.y * velocityScale,
                    angle, angleVel, 1.0f + size, 1.0f + size, sizeVel, 0.6f, 0.6f, 0.6f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnSmallGarbage(int count, float x, float y, float velocityX, float velocityY, float size, float sizeVel, float alphaVel) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f + rand.nextFloat() / 3.0f * 6.0f, CACHED_VECTOR);
            CACHED_VECTOR.add(velocityX * 0.6f, velocityY * 0.6f);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.001f, 0.001f) * 60.0f;
            float color = 0.7f;
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleGarbage0, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, 1.0f + size, 1.0f + size, sizeVel,
                    color, color, color, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnSmallGarbage(int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.02f + rand.nextFloat() / 3.0f * 6.0f, CACHED_VECTOR);
            CACHED_VECTOR.add(velocityX * 0.6f, velocityY * 0.6f);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.001f, 0.001f) * 60.0f;
            float sizeVel = 0.025f + rand.nextFloat() / 6.0f * 6.0f;
            float alphaVel = 0.002F * 60.0f;
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleGarbage0, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, 1.0f + size, 1.0f + size, sizeVel,
                    0.6f, 0.6f, 0.6f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnMediumGarbage(int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.04f + rand.nextFloat() / 8.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0005f - rand.nextFloat() / 2000.0f * 60.0f;
            float sizeVel = 0.46F + rand.nextFloat() / 8.0f * 6.0f;
            float alphaVel = 0.002F * 60.0f;
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleGarbage1, x, y, velocityX * 0.35f + CACHED_VECTOR.x, velocityY * 0.35f + CACHED_VECTOR.y,
                    angle, angleVel, size + 2.5f, size + 2.5f, sizeVel, 0.7f, 0.7f, 0.7f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnLargeGarbage(int count, Vector2f pos, Vector2f velocity, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f + rand.nextFloat() / 4.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0005f - rand.nextFloat() / 2000.0f * 60.0f;
            float sizeVel = 0.25F * 6.0f;
            float alphaVel = 0.001F * 60.0f;
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleGarbage2, pos.x, pos.y, velocity.x * 6.0f + CACHED_VECTOR.x, velocity.y * 6.0f + CACHED_VECTOR.y, angle,
                    angleVel, size + 7.0f, size + 7.0f, sizeVel, 0.9f, 0.9f, 0.9f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnShipOst(int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.2f + rand.nextFloat() / 3.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.05f, 0.05f) * 60.0f;
            float size1 = (2.0f + rand.nextFloat()) * size;
            float sizeVel = 0;
            float alphaVel = 0.06f;
            TextureRegister texture = rand.nextInt(2) == 0 ? TextureRegister.particleShipOst0 : TextureRegister.particleShipOst1;
            PARTICLE_POOL.getOrCreate(Particle::new).init(texture, x, y, velocityX * 12.0f + CACHED_VECTOR.x, velocityY * 12.0f + CACHED_VECTOR.y, angle, angleVel,
                    size1, size1, sizeVel, 0.5f, 0.5f, 0.5f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ALPHA_BLENDED);
        }
    }

    public static void spawnWeaponShoot(TextureRegister texture, Vector2f pos, float angle, float size, float r, float g, float b, float a) {
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleLight, pos.x, pos.y, 0, 0, 0, 0, size, size, 0, r, g, b, a, 0.05f * 60.0f,
                false, RenderLayer.DEFAULT_ADDITIVE);
        PARTICLE_POOL.getOrCreate(Particle::new).init(texture, pos.x, pos.y, 0, 0, angle, 0, size, size, 0, r, g, b, a, 0.05f * 60.0f, false,
                RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDamageSmoke(float x, float y, float size, float sizeVel, float velScale) {
        int count = rand.nextInt(4) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.05f + rand.nextFloat() / 3.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = 0.0F;
            float alphaVel = 0.015F * 60.0f;
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleSmoke1, x, y, CACHED_VECTOR.x * velScale, CACHED_VECTOR.y * velScale, angle, angleVel,
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
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleSmokeRing, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size, size, sizeVel,
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
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleExplosion, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size, size, sizeVel,
                    1.0f, 1.0f, 1.0f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ADDITIVE);
        }
    }

    public static void spawnExplosion(float x, float y, float size) {
        int count = rand.nextInt(2) + 1;
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 0.01f + rand.nextFloat() / 4.0f * 6.0f, CACHED_VECTOR);
            float angle = MathUtils.TWO_PI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.004f, 0.004f) * 60.0f;
            float sizeVel = 0.9F * 6.0f;
            float alphaVel = 0.014F * 60.0f;
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleExplosion, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size, size, sizeVel,
                    1.0f, 1.0f, 1.0f, 1.0f, alphaVel, false, RenderLayer.DEFAULT_ADDITIVE);
        }
    }

    public static void spawnShipEngineSmoke(float x, float y) {
        float rot = (rand.nextFloat() * MathUtils.TWO_PI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 100.0f * 60.0f;
        float sizeRandom = RandomHelper.randomFloat(rand, 0.5f, 1.0f);
        float sizeSpeed = 12.0f;
        float alphaSpeed = 2.7f;
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleSmoke2, x, y, 0, 0, rot, rotSpeed, 1.5f * sizeRandom, 1.5f * sizeRandom, sizeSpeed,
                0.5f, 0.5f, 0.5f, 0.75f, alphaSpeed, false, RenderLayer.BACKGROUND_ALPHA_BLENDED);
    }

    public static void spawnBeamDamage(Raycast raycast, float x, float y, float size, float sizeSpeed, Vector4f color) {
        Vector2 normal = raycast.getNormal();
        float rot = (float) Math.atan2(normal.x, -normal.y);
        float alphaSpeed = 6.0f;
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleBeamDamage, x, y, 0, 0, rot, 0, size, size, sizeSpeed,
                color.x, color.y, color.z, color.w, alphaSpeed, false, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDirectedSpark(Contact contact, Vector2 normal, float size, float r, float g, float b, float a) {
        Vector2 point = contact.getPoint();
        float rot = (float) Math.atan2(normal.x, -normal.y);
        rot += Math.PI / 2.0;
        float alphaSpeed = 6.0f;
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleDirectedSpark, (float) point.x, (float) point.y, 0, 0, rot, 0, size, size, 0.0f,
                r, g, b, a, alphaSpeed, false, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDirectedSplat(Contact contact, Vector2 normal, float size, float r, float g, float b, float a) {
        Vector2 point = contact.getPoint();
        float rot = (float) Math.atan2(normal.x, -normal.y);
        rot += Math.PI / 2.0;
        float alphaSpeed = 6.0f;
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleDirectedSplat, (float) point.x, (float) point.y, 0, 0, rot, 0, size, size, 0.0f,
                r, g, b, a, alphaSpeed, false, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDisableShield(float x, float y, float size, float sizeSpeed, float r, float g, float b, float a) {
        float rot = (rand.nextFloat() * MathUtils.TWO_PI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 20.0f * 60.0f;
        float alphaSpeed = 0.06f * 60.0f;
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleDisableShield, x, y, 0, 0, rot, rotSpeed, size, size, sizeSpeed,
                r, g, b, a, alphaSpeed, true, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDisableShield(float x, float y, float velocityX, float velocityY, float size, float r, float g, float b, float a) {
        float rot = (rand.nextFloat() * MathUtils.TWO_PI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 20.0f * 60.0f;
        float sizeSpeed = 6.0f;
        float alphaSpeed = 0.06f * 60.0f;
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleDisableShield, x, y, velocityX, velocityY, rot, rotSpeed, size, size, sizeSpeed,
                r, g, b, a, alphaSpeed, true, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnDisableShield(float x, float y, float size, float r, float g, float b, float a) {
        float rot = (rand.nextFloat() * MathUtils.TWO_PI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 20.0f * 60.0f;
        float sizeSpeed = 60.0f;
        float alphaSpeed = 0.06f * 60.0f;
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleDisableShield, x, y, 0, 0, rot, rotSpeed, size, size, sizeSpeed,
                r, g, b, a, alphaSpeed, true, RenderLayer.DEFAULT_ADDITIVE);
    }

    public static void spawnLight(float x, float y, float size, float sizeSpeed, float r, float g, float b, float a, float alphaSpeed, boolean alphaFromZero, RenderLayer renderLayer) {
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleLight, x, y, 0, 0, 0, 0, size, size, sizeSpeed, r, g, b, a,
                alphaSpeed, alphaFromZero, renderLayer);
    }

    public static void spawnLight(float x, float y, float velocityX, float velocityY, float size, float r, float g, float b, float a, float alphaSpeed, boolean alphaFromZero,
                                  RenderLayer position) {
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleLight, x, y, velocityX, velocityY, 0, 0, size, size, 0,
                r, g, b, a, alphaSpeed, alphaFromZero, position);
    }

    public static void spawnLight(float x, float y, float size, float r, float g, float b, float a, float alphaSpeed, boolean alphaFromZero, RenderLayer renderLayer) {
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleLight, x, y, 0, 0, 0, 0, size, size, 0, r, g, b, a,
                alphaSpeed, alphaFromZero, renderLayer);
    }

    public static void spawnLight(float x, float y, float size, float r, float g, float b, float a, RenderLayer renderLayer) {
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleLight, x, y, 0, 0, 0, 0, size, size, 0, r, g, b, a,
                0.5f * 60.0f, false, renderLayer);
    }

    public static void spawnEngineBack(float x, float y, float velocityX, float velocityY, float rot, float size, float alphaVel, float r, float g, float b, float a, boolean spawnSmoke) {
        float angleVel = 0;
        float sizeVel = -0.9f;
        PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleShipEngineBack, x, y, velocityX * 30.0f, velocityY * 30.0f, rot, angleVel, size, size, sizeVel,
                r, g, b, a, alphaVel, false, RenderLayer.BACKGROUND_ADDITIVE);

        if (spawnSmoke) {
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.2f, CACHED_VECTOR);
            alphaVel = 0.05f;
            rot = (rand.nextFloat() * MathUtils.TWO_PI);
            sizeVel = 7.0F;
            PARTICLE_POOL.getOrCreate(Particle::new).init(TextureRegister.particleSmoke2, x, y, CACHED_VECTOR.x, CACHED_VECTOR.y, rot, angleVel, 3.0f, 3.0f, sizeVel,
                    r, g, b, 0.075f, alphaVel, false, RenderLayer.BACKGROUND_ALPHA_BLENDED);
        }
    }
}
