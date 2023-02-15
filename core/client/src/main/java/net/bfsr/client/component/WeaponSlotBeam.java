package net.bfsr.client.component;

import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.Ship;
import net.bfsr.client.network.packet.common.PacketWeaponShoot;
import net.bfsr.client.particle.Beam;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.component.weapon.WeaponSlotBeamCommon;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.wreck.WreckCommon;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.util.TimeUtils;
import org.dyn4j.collision.narrowphase.Raycast;
import org.joml.Vector4f;

import java.util.Random;

public abstract class WeaponSlotBeam extends WeaponSlotBeamCommon {
    private final Beam beam;
    @Getter
    private final Texture texture;
    private final SoundRegistry[] shootSounds;

    protected WeaponSlotBeam(Ship ship, float beamMaxRange, BulletDamage damage, Vector4f beamColor, float shootTimerMax, float energyCost, float scaleX, float scaleY, TextureRegister texture,
                             SoundRegistry[] shootSounds) {
        super(ship, beamMaxRange, damage, beamColor, shootTimerMax, energyCost, scaleX, scaleY);
        this.texture = TextureLoader.getTexture(texture);
        this.shootSounds = shootSounds;
        this.beam = new Beam(this, ship);
    }

    @Override
    public void update() {
        beam.setLastValues();
        super.update();

        if (shootTimer > 0) {
            if (shootTimer <= shootTimerMax / 3.0f) {
                maxColor = false;
                if (beamColor.w > 0.0f) {
                    beamColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (beamColor.w < 0) beamColor.w = 0;
                }
            } else {
                if (!maxColor && beamColor.w < 1.0f) {
                    beamColor.w += 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (beamColor.w > 1.0f) beamColor.w = 1.0f;
                } else {
                    maxColor = true;
                }

                if (maxColor) {
                    beamColor.w = rand.nextFloat() / 3.0f + 0.66f;
                }

                ParticleSpawner.spawnLight(position.x, position.y, scale.x * 2.5f, beamColor.x, beamColor.y, beamColor.z, 0.6f * beamColor.w, RenderLayer.DEFAULT_ADDITIVE);
                ParticleSpawner.spawnBeam(position.x, position.y, rotation, 2.0f, beamColor.x, beamColor.y, beamColor.z, beamColor.w);
            }

            rayCast();

            if (beamColor.w > 0) {
                beam.update();
            }
        } else {
            if (beamColor.w > 0.0f) {
                beamColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                if (beamColor.w < 0) beamColor.w = 0;
            }
        }

        beam.updateEffects();
    }

    @Override
    public void updatePos() {
        super.updatePos();
        if (shootTimer > 0 && beamColor.w > 0) {
            beam.updatePosition();
        }
    }

    @Override
    protected void shoot() {
        Core.get().sendPacket(new PacketWeaponShoot(ship.getId(), id));
    }

    public void clientShoot() {
        float energy = ship.getReactor().getEnergy();
        playSound();
        shootTimer = shootTimerMax;
        ship.getReactor().setEnergy(energy - energyCost);
        beam.init();
    }

    protected void playSound() {
        if (shootSounds != null) {
            int size = shootSounds.length;
            Random rand = world.getRand();
            SoundRegistry sound = shootSounds[rand.nextInt(size)];
            SoundSourceEffect source = new SoundSourceEffect(sound, position.x, position.y);
            Core.get().getSoundManager().play(source);
        }
    }

    @Override
    protected void onDamageShip(Raycast raycast, float hitX, float hitY, float sizeSpeed) {
        Random rand = world.getRand();
        ParticleSpawner.spawnBeamDamage(raycast, hitX, hitY, scale.x, sizeSpeed, beamColor);

        ShieldCommon shield = ship.getShield();
        if (shield == null || shield.getShield() <= 0) {
            if (rand.nextInt(5) == 0) {
                Hull hull = ship.getHull();
                float velocityX = ship.getVelocity().x * 0.005f;
                float velocityY = ship.getVelocity().y * 0.005f;
                if (hull.getHull() / hull.getMaxHull() < 0.5f && rand.nextInt(50) == 0) {
                    RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                    ParticleSpawner.spawnShipOst(1, hitX, hitY, velocityX + ParticleSpawner.CACHED_VECTOR.x,
                            velocityY + ParticleSpawner.CACHED_VECTOR.y, 0.5f);
                }
                RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), hitX, hitY, velocityX + ParticleSpawner.CACHED_VECTOR.x,
                        velocityY + ParticleSpawner.CACHED_VECTOR.y, 2.0f * rand.nextFloat());
            }
        }
    }

    @Override
    protected void onDamageWreck(Raycast raycast, float hitX, float hitY, float sizeSpeed, WreckCommon wreck) {
        Random rand = world.getRand();
        ParticleSpawner.spawnBeamDamage(raycast, hitX, hitY, scale.x, sizeSpeed, beamColor);
        if (rand.nextInt(5) == 0) {
            float velocityX = wreck.getVelocity().x * 0.005f;
            float velocityY = wreck.getVelocity().y * 0.005f;
            if (rand.nextInt(50) == 0) {
                RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                ParticleSpawner.spawnShipOst(1, hitX, hitY, velocityX + ParticleSpawner.CACHED_VECTOR.x,
                        velocityY + ParticleSpawner.CACHED_VECTOR.y, 0.5f);
            }
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
            ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), hitX, hitY, velocityX + ParticleSpawner.CACHED_VECTOR.x,
                    velocityY + ParticleSpawner.CACHED_VECTOR.y, 2.0f * rand.nextFloat());
        }
    }

    @Override
    protected void onDamageObject(Raycast raycast, float hitX, float hitY, float sizeSpeed, CollisionObject collisionObject) {
        Random rand = world.getRand();
        ParticleSpawner.spawnBeamDamage(raycast, hitX, hitY, scale.x, sizeSpeed, beamColor);
        if (rand.nextInt(5) == 0) {
            float velocityX = collisionObject.getVelocity().x * 0.005f;
            float velocityY = collisionObject.getVelocity().y * 0.005f;
            if (rand.nextInt(50) == 0) {
                RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                ParticleSpawner.spawnShipOst(1, hitX, hitY, velocityX + ParticleSpawner.CACHED_VECTOR.x, velocityY + ParticleSpawner.CACHED_VECTOR.y, 0.5f);
            }
            RotationHelper.angleToVelocity(MathUtils.TWO_PI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
            ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), hitX, hitY, velocityX + ParticleSpawner.CACHED_VECTOR.x,
                    velocityY + ParticleSpawner.CACHED_VECTOR.y, 2.0f * rand.nextFloat());
        }
    }

    @Override
    public void render() {
        SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                scale.x, scale.y, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
    }

    public void renderAdditive() {
        if (shootTimer > 0 && beamColor.w > 0) {
            beam.render();
        }
    }
}
