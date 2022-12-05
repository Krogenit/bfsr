package net.bfsr.client.particle;

import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.server.PacketSpawnParticle;
import net.bfsr.server.MainServer;
import net.bfsr.util.RandomHelper;
import net.bfsr.world.World;
import net.bfsr.world.WorldServer;
import org.dyn4j.collision.narrowphase.Raycast;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class ParticleSpawner {

    private static final Random rand = new Random();
    private static final Vector2f angleToVelocity = new Vector2f();

    public static void spawnDestroyShipSmall(Ship ship) {
        Vector2f scale = ship.getScale();
        Vector2f pos = ship.getPosition();
        Vector2f velocity = ship.getVelocity();
        float baseSize = 40f + scale.x * 0.25f;
        World w = ship.getWorld();
        Random rand = w.getRand();
        if (w.isRemote()) {
            ParticleRenderer ps = w.getParticleRenderer();
            ship.setDead(true);
            Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion1, pos));
            spawnShockwave(0, pos, baseSize + 30f);
            for (int i = 0; i < 8; i++)
                spawnMediumGarbage(1, new Vector2f(pos).add(new Vector2f(-scale.x / 2.25f + rand.nextInt((int) (scale.x / 1.25f)), -scale.y / 2.25f + rand.nextInt((int) (scale.y / 1.25f)))),
                        new Vector2f(velocity).mul(0.25f).add(RotationHelper.angleToVelocity(rand.nextFloat() * RotationHelper.TWOPI, 70f)),
                        baseSize);
            float size = (scale.x + scale.y) * 1.1f;
            spawnSpark(pos, size);
            spawnLight(pos, size, 4f * 60f, new Vector4f(1, 0.5f, 0.4f, 1f), 0.05f * 60f, true, EnumParticlePositionType.Default);
            spawnRocketShoot(pos, size);
            spawnSmallGarbage(4 + rand.nextInt(10), pos.x, pos.y, velocity.x * 0.25f, velocity.y * 0.25f, 1f);
        } else {
            spawnDamageDerbis(w, rand.nextInt(3), pos.x, pos.y, velocity.x * 0.25f, velocity.y * 0.25f, 1f);
            spawnDamageWrecks(w, rand.nextInt(2), pos, new Vector2f(velocity).mul(0.25f));
            Vector2 bodyVelocity = new Vector2(ship.getBody().getLinearVelocity());
            float rot = ship.getRotation();
            Vector2 velocity1 = new Vector2(rot).negate().multiply(30f).add(new Vector2(bodyVelocity).multiply(0.4f));
            if (rand.nextInt(2) == 0) {
                spawnShipWreck(ship, 0, pos, rot, velocity1, 0.02f, 750f);
            }

            if (rand.nextInt(2) == 0) {
                bodyVelocity.negate();
                velocity1 = new Vector2(rot).multiply(30f).add(new Vector2(bodyVelocity).multiply(0.4f));
                spawnShipWreck(ship, 1, pos, rot, velocity1, 0.02f, 750f);
            }

        }
    }

    public static void spawnShipWreck(Ship s, int textureOffset, Vector2f pos, float angle, Vector2 velocity, float alphaVel, float wreckLifeTime) {
        Vector2f velocity1 = new Vector2f((float) velocity.x, (float) velocity.y);
        float angleVel = (-0.005f + rand.nextFloat() / 200f) * 60f;
        Vector2f size1 = new Vector2f(s.getScale().x, s.getScale().y);
        float sizeVel = 0.f;
        Vector4f color = new Vector4f(0.5f, 0.5f, 0.5f, 1f);
        ParticleWreck p = new ParticleWreck(s.getWorld().getNextId(), textureOffset, s, new Vector2f(pos), velocity1, angle, angleVel, size1, sizeVel, color, alphaVel, wreckLifeTime);
        MainServer.getServer().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnParticle(p), pos, WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public static void spawnDamageDerbis(World world, int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity1 = RotationHelper.angleToVelocity(rand.nextFloat() * RotationHelper.TWOPI, 40f + rand.nextFloat() * 20f);
            velocity1.add(velocityX, velocityY).mul(0.7f);
            float angle = rand.nextFloat() * RotationHelper.TWOPI;
            float angleVel = (-0.005f + rand.nextFloat() / 200f) * 60f;
            Vector4f color = new Vector4f(0.5f, 0.5f, 0.5f, 1f);
            float size2 = (1F - rand.nextFloat() / 3F) * 20f * size;
            Vector2f size1 = new Vector2f(size2, size2);
            float sizeVel = 0.f;
            float alphaVel = 0.1f;//RandomHelper.randomFloat(rand, 0.0003f, 0.0006f) * 60f;
            boolean isFire = rand.nextInt(3) == 0;
            boolean isFireExplosion = isFire && rand.nextInt(5) == 0;
            Vector2f pos = new Vector2f(x, y);
            ParticleWreck p = new ParticleWreck(world.getNextId(), rand.nextInt(6), false, isFire, isFireExplosion, pos, velocity1, angle, angleVel, size1, sizeVel, color, alphaVel);
            MainServer.getServer().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnParticle(p), pos, WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public static void spawnDamageWrecks(World world, int count, Vector2f pos, Vector2f velocity) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity1 = RotationHelper.angleToVelocity(rand.nextFloat() * RotationHelper.TWOPI, 40f + rand.nextFloat() * 20f).add(new Vector2f(velocity).mul(0.7f));
            float angle = rand.nextFloat() * RotationHelper.TWOPI;
            float angleVel = (-0.005f + rand.nextFloat() / 200f) * 60f;
            Vector4f color = new Vector4f(0.5f, 0.5f, 0.5f, 1f);
            float size = (1F - rand.nextFloat() / 3F) * 40f;
            Vector2f size1 = new Vector2f(size, size);
            float sizeVel = 0.f;
            float alphaVel = 0.04f;//RandomHelper.randomFloat(rand, 0.00015f, 0.0003f) * 60f;
            boolean isFireExplosion = rand.nextInt(4) == 0;
            ParticleWreck p = new ParticleWreck(world.getNextId(), rand.nextInt(3), true, true, isFireExplosion, new Vector2f(pos), velocity1, angle, angleVel, size1, sizeVel, color, alphaVel);
            MainServer.getServer().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnParticle(p), pos, WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public static void spawnBeam(WeaponSlotBeam slot, boolean isSmall) {
        new ParticleBeam(slot, isSmall, TextureRegister.particleBeam);
    }

    public static Particle spawnBeam(Vector2f pos, float rot, Vector2f size, Vector4f color) {
        Vector2f velocity = new Vector2f();
        float angleVel = 0.0F;
        float sizeVel = 0f;
        float alphaVel = 0.001f;
        Vector4f color1 = new Vector4f(color);
//		color1.w *= 2f;
        return new Particle(TextureRegister.particleBeam, new Vector2f(pos), velocity, rot, angleVel, new Vector2f(size), sizeVel, color1, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
    }

    public static Particle spawnBeamEffect(WeaponSlotBeam slot) {
        return new ParticleBeamEffect(slot, TextureRegister.particleBeamEffect);
    }

    public static void spawnLightingIon(Vector2f pos, float size) {
        int count = rand.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            Vector2f velocity = new Vector2f();
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0.0F;
            Vector4f color = new Vector4f(0.75F, 0.75F, 1, 1.5f);
            Vector2f size1 = new Vector2f(size, size);
            float sizeVel = 32F;
            float alphaVel = 8F;
            new Particle(TextureRegister.particleLighting, new Vector2f(pos).add(RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), size / 4f)), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        }
    }

    public static void spawnRocketShoot(Vector2f pos, float size) {
        for (int a = 0; a < 2; a++) {
            Vector2f velocity = new Vector2f(0, 0);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0.0F;
            Vector4f color = new Vector4f(1f, 1f, 0.5f, 1f);
            Vector2f size1 = new Vector2f(size, size);
            float sizeVel = 1.5F * 60f;
            float alphaVel = 0.025F * 60f;
            new Particle(TextureRegister.particleRocketEffect, new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        }

        for (int a = 0; a < 1; a++) {
            Vector2f velocity = new Vector2f(0, 0);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0.0F;
            Vector4f color = new Vector4f(0.3f, 0.3f, 0.3f, 1f);
            Vector2f size1 = new Vector2f(size, size);
            float sizeVel = 1.5F * 60f;
            float alphaVel = 0.02F * 60f;
            new Particle(TextureRegister.particleRocketSmoke, new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);
        }
    }

    public static void spawnShockwave(int type, Vector2f pos, float size) {
        Vector2f velocity = new Vector2f(0, 0);
        float angle = RotationHelper.TWOPI * rand.nextFloat();
        float angleVel = 0.0F;
        Vector4f color = new Vector4f(0.5F, 0.5F, 0.5F, 1);
        Vector2f size1 = new Vector2f(size, size);
        float sizeVel = type == 1 ? 0.8F * 60f : type == 2 ? 1.5F * 60f : 6F * 60f;
        float alphaVel = type == 1 ? 0.004F * 60f : type == 2 ? 0.006F * 60f : 0.02F * 60f;
        new Particle(TextureRegister.values()[TextureRegister.particleSockwaveSmall.ordinal() + type], new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
    }

    public static void spawnSpark(Vector2f pos, float size, float sizeVel) {
        for (int i = 0; i < 3; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), .2f * 60f);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0;
            Vector4f color = new Vector4f(1.0f, 0.5f, 0.0f, 1.0f);
            Vector2f size1 = new Vector2f(size / 2f, size / 2f);
            float alphaVel = .03f * 60f;

            new Particle(TextureRegister.particleExplosion, new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        }

        for (int i = 0; i < 2; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), .2f * 60f);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0;
            Vector4f color = new Vector4f(1.0f, 0.5f, 0.0f, 1.0f);
            Vector2f size1 = new Vector2f(size, size);
            sizeVel = 0;
            float alphaVel = .04f * 60f;

            new Particle(TextureRegister.values()[TextureRegister.particleSpark0.ordinal() + rand.nextInt(4)], new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        }
    }

    public static void spawnSpark(Vector2f pos, float size) {
        for (int i = 0; i < 3; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), .2f * 60f);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0;
            Vector4f color = new Vector4f(1.0f, 0.5f, 0.0f, 1.0f);
            Vector2f size1 = new Vector2f(size / 2f, size / 2f);
            float sizeVel = 1f * 60f;
            float alphaVel = .03f * 60f;

            new Particle(TextureRegister.particleExplosion, new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        }

        for (int i = 0; i < 2; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), .2f * 60f);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0;
            Vector4f color = new Vector4f(1.0f, 0.5f, 0.0f, 1.0f);
            Vector2f size1 = new Vector2f(size, size);
            float sizeVel = 0;
            float alphaVel = .04f * 60f;

            new Particle(TextureRegister.values()[TextureRegister.particleSpark0.ordinal() + rand.nextInt(4)], new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        }
    }

    public static void spawnSmallGarbage(int count, float x, float y, float velocityScale, float size) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity1 = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.2f + rand.nextFloat() / 3f * 60f).mul(velocityScale);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.001f, 0.001f) * 60f;
            Vector4f color = new Vector4f(0.6f, 0.6f, 0.6f, 1f);
            Vector2f size1 = new Vector2f(10f + size, 10f + size);
            float sizeVel = 0.25f + rand.nextFloat() / 6f * 60f;
            float alphaVel = 0.004F * 60f;
            new Particle(TextureRegister.particleGarbage0, new Vector2f(x, y), velocity1, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);

        }
    }

    public static void spawnSmallGarbage(int count, float x, float y, float velocityX, float velocityY, float size, float sizeVel, float alphaVel) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity1 = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.2f + rand.nextFloat() / 3f * 60f);
            velocity1.add(velocityX * 0.6f, velocityY * 0.6f);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.001f, 0.001f) * 60f;
            float c = 0.7f;
            Vector4f color = new Vector4f(c, c, c, 1f);
            Vector2f size1 = new Vector2f(10f + size, 10f + size);
            new Particle(TextureRegister.particleGarbage0, new Vector2f(x, y), velocity1, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);

        }
    }

    public static void spawnSmallGarbage(int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity1 = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.2f + rand.nextFloat() / 3f * 60f);
            velocity1.add(velocityX * 0.6f, velocityY * 0.6f);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.001f, 0.001f) * 60f;
            Vector4f color = new Vector4f(0.6f, 0.6f, 0.6f, 1f);
            Vector2f size1 = new Vector2f(10f + size, 10f + size);
            float sizeVel = 0.25f + rand.nextFloat() / 6f * 60f;
            float alphaVel = 0.002F * 60f;
            new Particle(TextureRegister.particleGarbage0, new Vector2f(x, y), velocity1, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);
        }
    }

    public static void spawnMediumGarbage(int count, Vector2f pos, Vector2f velocity, float size) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity1 = new Vector2f(velocity).mul(0.35f).add(RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.04f + rand.nextFloat() / 8f * 60f));
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0.0005f - rand.nextFloat() / 2000f * 60f;
            Vector4f color = new Vector4f(0.7f, 0.7f, 0.7f, 1f);
            Vector2f size1 = new Vector2f(size + 25f, size + 25f);
            float sizeVel = 4.6F + rand.nextFloat() / 8f * 60f;
            float alphaVel = 0.002F * 60f;
            new Particle(TextureRegister.particleGarbage1, new Vector2f(pos), velocity1, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);
        }
    }

    public static void spawnLargeGarbage(int count, Vector2f pos, Vector2f velocity, float size) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity1 = new Vector2f(velocity).mul(6f).add(RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.2f + rand.nextFloat() / 4f * 60f));
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0.0005f - rand.nextFloat() / 2000f * 60f;
            Vector4f color = new Vector4f(0.9f, 0.9f, 0.9f, 1f);
            Vector2f size1 = new Vector2f(size + 70f, size + 70f);
            float sizeVel = 0.25F * 60f;
            float alphaVel = 0.001F * 60f;
            new Particle(TextureRegister.particleGarbage2, new Vector2f(pos), velocity1, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);
        }
    }

    public static void spawnShipOst(int count, Vector2f pos, Vector2f velocity, float size) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity1 = new Vector2f(velocity).mul(12f).add(RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.2f + rand.nextFloat() / 3f * 60f));
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.05f, 0.05f) * 60f;
            Vector4f color = new Vector4f(0.5f, 0.5f, 0.5f, 1f);
            float size1 = (20f + rand.nextFloat() * 10f) * size;
            Vector2f size2 = new Vector2f(size1, size1);
            float sizeVel = 0;
            float alphaVel = 0.001F * 60f;
            TextureRegister texture = rand.nextInt(2) == 0 ? TextureRegister.particleShipOst0 : TextureRegister.particleShipOst1;
            new Particle(texture, new Vector2f(pos), velocity1, angle, angleVel, size2, sizeVel, color, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);
        }
    }

    public static void spawnWeaponShoot(TextureRegister texture, Vector2f pos, float angle, float size, Vector4f color) {
        new Particle(TextureRegister.particleLight, new Vector2f(pos), new Vector2f(0, 0), 0, 0, new Vector2f(size, size), 0, new Vector4f(color), 0.05f * 60f, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        new Particle(texture, new Vector2f(pos), new Vector2f(0, 0), angle, 0, new Vector2f(size, size), 0, new Vector4f(color), 0.05f * 60f, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
    }

    public static void spawnDamageSmoke(Vector2f pos, float size, float sizeVel, float velScale) {
        int count = rand.nextInt(4) + 1;
        for (int i = 0; i < count; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.05f + rand.nextFloat() / 3f * 60f).mul(velScale);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0.0F;
            Vector4f color = new Vector4f(1f, 1f, 1f, 0.6f);
            Vector2f size1 = new Vector2f(size, size);
            float alphaVel = 0.015F * 60f;
            Particle p = new Particle(TextureRegister.particleSmoke1, new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);
        }
    }

    public static void spawnDamageSmoke(Vector2f pos, float size) {
        int count = rand.nextInt(4) + 1;
        for (int i = 0; i < count; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.05f + rand.nextFloat() / 3f * 60f);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = 0.0F;
            Vector4f color = new Vector4f(0.75f, 0.75f, 0.75f, 0.75f);
            Vector2f size1 = new Vector2f(size, size);
            float sizeVel = 0.75F * 60f;
            float alphaVel = 0.015F * 60f;
            Particle p = new Particle(TextureRegister.particleSmokeRing, new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.AlphaBlended);
        }
    }

    public static void spawnExplosion(Vector2f pos, float size, float sizeVel) {
        int count = rand.nextInt(2) + 1;
        for (int i = 0; i < count; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.01f + rand.nextFloat() / 4f * 60f);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.004f, 0.004f) * 60f;
            Vector4f color = new Vector4f(1f, 1f, 1f, 1f);
            Vector2f size1 = new Vector2f(size, size);
            float alphaVel = 0.014F * 60f;
            new Particle(TextureRegister.particleExplosion, new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        }
    }

    public static void spawnExplosion(Vector2f pos, float size) {
        int count = rand.nextInt(2) + 1;
        for (int i = 0; i < count; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 0.01f + rand.nextFloat() / 4f * 60f);
            float angle = RotationHelper.TWOPI * rand.nextFloat();
            float angleVel = RandomHelper.randomFloat(rand, -0.004f, 0.004f) * 60f;
            Vector4f color = new Vector4f(1f, 1f, 1f, 1f);
            Vector2f size1 = new Vector2f(size, size);
            float sizeVel = 0.9F * 60f;
            float alphaVel = 0.014F * 60f;
            new Particle(TextureRegister.particleExplosion, new Vector2f(pos), velocity, angle, angleVel, size1, sizeVel, color, alphaVel, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
        }
    }

    public static void spawnShipEngineSmoke(Vector2f pos) {
        float rot = (rand.nextFloat() * RotationHelper.TWOPI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 100f * 60f;
        Vector4f color = new Vector4f(0.5f, 0.5f, 0.5f, 0.75f);
        float sizeRandom = RandomHelper.randomFloat(rand, 0.5f, 1.0f);
        Vector2f size = new Vector2f(15f * sizeRandom, 15f * sizeRandom);
        float sizeSpeed = 120f;
        float alphaSpeed = 0.045f * 60f;
        Vector2f velocity = new Vector2f(0, 0);
        new Particle(TextureRegister.particleSmoke2, new Vector2f(pos), velocity, rot, rotSpeed, size, sizeSpeed, color, alphaSpeed, 0.001f, false, false, EnumParticlePositionType.Background, EnumParticleRenderType.AlphaBlended);
    }

    public static void spawnBeamDamage(Raycast raycast, float size, float sizeSpeed, Vector4f color) {
        Vector2 point = raycast.getPoint();
        Vector2 normal = raycast.getNormal();
        Vector2f pos = new Vector2f((float) point.x, (float) point.y);
        float rot = (float) Math.atan2(normal.x, -normal.y);
//		rot += Math.PI / 2.0;
        float alphaSpeed = 6f;
        new Particle(TextureRegister.particleBeamDamage, pos, new Vector2f(0, 0), rot, 0, new Vector2f(size, size), sizeSpeed, new Vector4f(color), alphaSpeed, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
    }

    public static void spawnDirectedSpark(Contact contact, Vector2 normal, float size, Vector4f color) {
        Vector2 point = contact.getPoint();
        Vector2f pos = new Vector2f((float) point.x, (float) point.y);
        float rot = (float) Math.atan2(normal.x, -normal.y);
        rot += Math.PI / 2.0;
        float alphaSpeed = 6f;
        new Particle(TextureRegister.particleDirectedSpark, pos, new Vector2f(0, 0), rot, 0, new Vector2f(size, size), 0f, new Vector4f(color), alphaSpeed, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
    }

    public static void spawnDirectedSplat(Contact contact, Vector2 normal, float size, Vector4f color) {
        Vector2 point = contact.getPoint();
        Vector2f pos = new Vector2f((float) point.x, (float) point.y);
        float rot = (float) Math.atan2(normal.x, -normal.y);
        rot += Math.PI / 2.0;
        float alphaSpeed = 6f;
        new Particle(TextureRegister.particleDirectedSplat, pos, new Vector2f(0, 0), rot, 0, new Vector2f(size, size), 0f, new Vector4f(color), alphaSpeed, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
    }

    public static void spawnDisableShield(Vector2f pos, float size, float sizeSpeed, Vector4f color) {
        float rot = (rand.nextFloat() * RotationHelper.TWOPI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 20f * 60f;
        float alphaSpeed = 0.06f * 60f;
        new Particle(TextureRegister.particleDisableShield, new Vector2f(pos), new Vector2f(0, 0), rot, rotSpeed, new Vector2f(size, size), sizeSpeed, new Vector4f(color), alphaSpeed, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
    }

    public static void spawnDisableShield(Vector2f pos, Vector2f velocity, float size, Vector4f color) {
        float rot = (rand.nextFloat() * RotationHelper.TWOPI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 20f * 60f;
        float sizeSpeed = 60f;
        float alphaSpeed = 0.06f * 60f;
        new Particle(TextureRegister.particleDisableShield, new Vector2f(pos), new Vector2f(velocity), rot, rotSpeed, new Vector2f(size, size), sizeSpeed, new Vector4f(color), alphaSpeed, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
    }

    public static void spawnDisableShield(Vector2f pos, float size, Vector4f color) {
        float rot = (rand.nextFloat() * RotationHelper.TWOPI);
        float rotSpeed = (rand.nextFloat() - 0.5f) / 20f * 60f;
        float sizeSpeed = 60f;
        float alphaSpeed = 0.06f * 60f;
        new Particle(TextureRegister.particleDisableShield, new Vector2f(pos), new Vector2f(0, 0), rot, rotSpeed, new Vector2f(size, size), sizeSpeed, new Vector4f(color), alphaSpeed, 0.001f, true, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
    }

    public static void spawnLight(Vector2f pos, float size, float sizeSpeed, Vector4f color, float alphaSpeed, boolean alphaFromZero, EnumParticlePositionType position) {
        new Particle(TextureRegister.particleLight, new Vector2f(pos), new Vector2f(0, 0), 0, 0, new Vector2f(size, size), sizeSpeed, new Vector4f(color), alphaSpeed, 0.001f, alphaFromZero, false, position, EnumParticleRenderType.Additive);
    }

    public static void spawnLight(Vector2f pos, Vector2f velocity, float size, Vector4f color, float alphaSpeed, boolean alphaFromZero, EnumParticlePositionType position) {
        new Particle(TextureRegister.particleLight, new Vector2f(pos), new Vector2f(velocity), 0, 0, new Vector2f(size, size), 0, new Vector4f(color), alphaSpeed, 0.001f, alphaFromZero, false, position, EnumParticleRenderType.Additive);
    }

    public static void spawnLight(Vector2f pos, float size, Vector4f color, float alphaSpeed, boolean alphaFromZero, EnumParticlePositionType position) {
        new Particle(TextureRegister.particleLight, new Vector2f(pos), new Vector2f(0, 0), 0, 0, new Vector2f(size, size), 0, new Vector4f(color), alphaSpeed, 0.001f, alphaFromZero, false, position, EnumParticleRenderType.Additive);
    }

    public static void spawnLight(Vector2f pos, float size, Vector4f color, EnumParticlePositionType position) {
        new Particle(TextureRegister.particleLight, new Vector2f(pos), new Vector2f(0, 0), 0, 0, new Vector2f(size, size), 0, new Vector4f(color), 0.5f * 60f, 0.001f, false, false, position, EnumParticleRenderType.Additive);
    }

    public static void spawnEngineBack(Vector2f pos, Vector2f velocity, float rot, float size, float alphaVel, Vector4f color, boolean spawnSmoke) {
        float angleVel = 0;
        float sizeVel = -.15f * 60f;
        Vector2f size1 = new Vector2f(size, size);
        new Particle(TextureRegister.particleShipEngineBack, new Vector2f(pos), new Vector2f(velocity).mul(0.5f * 60f), rot, angleVel, size1, sizeVel, new Vector4f(color), alphaVel, 0.001f, false, false, EnumParticlePositionType.Background, EnumParticleRenderType.Additive);

        if (spawnSmoke) {
            Vector4f color1 = new Vector4f(color.x, color.y, color.z, 0.075f);
            Vector2f velocity1 = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 12f);
            alphaVel = 0.05f;
            rot = (rand.nextFloat() * RotationHelper.TWOPI);
            sizeVel = 70F;
//			size1 = new Vector2f(0.1F * 400f, 0.1F * 400f);
            size1 = new Vector2f(30f, 30f);
            new Particle(TextureRegister.particleSmoke2, new Vector2f(pos), velocity1, rot, angleVel, size1, sizeVel, color1, alphaVel, 0.001f, false, false, EnumParticlePositionType.Background, EnumParticleRenderType.AlphaBlended);
        }
    }
}
