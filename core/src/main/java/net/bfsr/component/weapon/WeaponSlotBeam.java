package net.bfsr.component.weapon;

import net.bfsr.client.particle.*;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.collision.filter.BeamFilter;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.Shield;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.common.PacketWeaponShoot;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.WorldServer;
import org.dyn4j.collision.narrowphase.Raycast;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.World;
import org.dyn4j.world.result.RaycastResult;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class WeaponSlotBeam extends WeaponSlot {
    private final Vector2 start = new Vector2();
    private final Vector2 end = new Vector2();
    private final BeamFilter filter = new BeamFilter(null);
    private final float beamMaxRange;
    private float currentBeamRange;
    private boolean maxColor, beamParticleSpawned;
    private final Vector2f collisionPoint = new Vector2f();
    private final BulletDamage damage;
    private final Vector4f beamColor;
    private List<Particle> particlesEffects;
    private final Random rand;

    protected WeaponSlotBeam(Ship ship, float beamMaxRange, BulletDamage damage, Vector4f beamColor, float shootTimerMax, float energyCost, Vector2f scale, TextureRegister texture, SoundRegistry[] shootSounds) {
        super(ship, shootSounds, shootTimerMax, energyCost, Float.MAX_VALUE, 0.0f, scale, texture);
        this.beamMaxRange = beamMaxRange;
        this.damage = damage;
        this.beamColor = beamColor;
        this.beamColor.w = 0.0f;
        rand = world.getRand();

        if (world.isRemote()) {
            particlesEffects = new ArrayList<>();
        }
    }

    @Override
    public void update() {
        super.update();
        if (world.isRemote()) {
            if (shootTimer > 0) {
                if (shootTimer <= shootTimerMax / 3.0f) {
                    maxColor = false;
                    if (beamColor.w > 0.0f) {
                        beamColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    }
                } else {
                    if (!maxColor && beamColor.w < 1.0f) {
                        beamColor.w += 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    } else {
                        maxColor = true;
                    }

                    if (maxColor) {
                        beamColor.w = rand.nextFloat() / 3.0f + 0.66f;
                    }

                    ParticleSpawner.spawnLight(position, scale.x * 2.5f, new Vector4f(beamColor.x, beamColor.y, beamColor.z, 0.6f * beamColor.w), EnumParticlePositionType.Default);
                    float alphaSpeed = 6.0f;
                    float size = 2.0f;
                    float sizeSpeed = 30.0f;
                    new Particle(TextureRegister.particleBeamDamage, position, RotationHelper.angleToVelocity(rotate, 10.0f), rotate, 0, new Vector2f(size, size), sizeSpeed, new Vector4f(beamColor), alphaSpeed, 0.001f, false, false, EnumParticlePositionType.Default, EnumParticleRenderType.Additive);
                }

                rayCast();

                if (beamColor.w > 0) {
                    if (!beamParticleSpawned) {
                        ParticleSpawner.spawnBeam(this, false);
                        ParticleSpawner.spawnBeam(this, true);
                        beamParticleSpawned = true;
                    }

                    while (particlesEffects.size() < currentBeamRange / 90.0f) {
                        Particle p = ParticleSpawner.spawnBeamEffect(this);
                        particlesEffects.add(p);
                    }
                }
            } else {
                if (beamColor.w > 0.0f) {
                    beamColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                }
            }

            for (int i = 0; i < particlesEffects.size(); i++) {
                Particle p = particlesEffects.get(i);
                if (p.isDead()) {
                    particlesEffects.remove(i);
                    i--;
                }
            }

        } else {
            if (shootTimer > 0) {
                if (shootTimer <= shootTimerMax / 3.0f) {
                    maxColor = false;
                    if (beamColor.w > 0.0f) {
                        beamColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    }
                } else {
                    if (!maxColor && beamColor.w < 1.0f) {
                        beamColor.w += 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    } else {
                        maxColor = true;
                    }

                    if (maxColor) {
                        beamColor.w = world.getRand().nextFloat() / 3.0f + 0.66f;
                    }
                }

                rayCast();
            } else {
                if (beamColor.w > 0.0f) {
                    beamColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                }
            }
        }
    }

    public void shoot() {
        float energy = ship.getReactor().getEnergy();
        if (shootTimer <= 0 && energy >= energyCost) {
            if (world.isRemote()) {
                Core.getCore().sendPacket(new PacketWeaponShoot(ship.getId(), id));
            } else {
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketWeaponShoot(ship.getId(), id), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
                shootTimer = shootTimerMax;
                ship.getReactor().setEnergy(energy - energyCost);
            }
        }
    }

    public void clientShoot() {
        beamParticleSpawned = false;
        float energy = ship.getReactor().getEnergy();
        playSound();
        shootTimer = shootTimerMax;
        ship.getReactor().setEnergy(energy - energyCost);
    }

    private void rayCast() {
        World<Body> physicWorld = world.getPhysicWorld();

        float cos = ship.getCos();
        float sin = ship.getSin();
        float startRange = -scale.x / 2.0f;

        start.x = cos * startRange + position.x;
        start.y = sin * startRange + position.y;
        end.x = start.x + cos * beamMaxRange;
        end.y = start.y + sin * beamMaxRange;
        collisionPoint.x = 0;
        collisionPoint.y = 0;
        filter.setUserData(ship);
        Ray ray = new Ray(start, new Vector2(cos, sin));
        DetectFilter<Body, BodyFixture> detectFilter = new DetectFilter<>(true, true, filter);
        RaycastResult<Body, BodyFixture> result = physicWorld.raycastClosest(ray, beamMaxRange, detectFilter);
        if (result != null) {
            Body body = result.getBody();
            Raycast raycast = result.getRaycast();
            Vector2 point = raycast.getPoint();
            collisionPoint.x = (float) point.x;
            collisionPoint.y = (float) point.y;
            currentBeamRange = (float) raycast.getDistance();

            Object userData = body.getUserData();

            if (userData != null) {
                float sizeSpeed = 30.0f;
                if (userData instanceof Ship ship) {
                    ship.attackShip(damage, this.ship, collisionPoint, ship.getFaction() == this.ship.getFaction() ? beamColor.w / 2.0f * 60.0f * TimeUtils.UPDATE_DELTA_TIME :
                            beamColor.w * 60.0f * TimeUtils.UPDATE_DELTA_TIME);
                    if (world.isRemote()) {
                        Random rand = world.getRand();
                        ParticleSpawner.spawnBeamDamage(raycast, scale.x, sizeSpeed, beamColor);

                        Shield shield = ship.getShield();
                        if (shield == null || shield.getShield() <= 0) {
                            if (rand.nextInt(5) == 0) {
                                Hull hull = ship.getHull();
                                Vector2 pos1 = raycast.getPoint();
                                Vector2f pos = new Vector2f((float) pos1.x, (float) pos1.y);
                                Vector2f velocity = new Vector2f(ship.getVelocity()).mul(0.005f);
                                if (hull.getHull() / hull.getMaxHull() < 0.5f && rand.nextInt(50) == 0) {
                                    Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
                                    ParticleSpawner.spawnShipOst(1, pos, new Vector2f(velocity).add(angletovel), 0.5f);
                                }
                                Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
                                ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), pos.x, pos.y, velocity.x + angletovel.x, velocity.y + angletovel.y, 2.0f * rand.nextFloat());
                            }
                        }
                    }
                } else if (userData instanceof ParticleWreck wreck) {
                    wreck.damage(damage.getBulletDamageHull() * beamColor.w);
                    if (world.isRemote()) {
                        Random rand = world.getRand();
                        ParticleSpawner.spawnBeamDamage(raycast, scale.x, sizeSpeed, beamColor);
                        if (rand.nextInt(5) == 0) {
                            Vector2 pos1 = raycast.getPoint();
                            Vector2f pos = new Vector2f((float) pos1.x, (float) pos1.y);
                            Vector2f velocity = new Vector2f(wreck.getVelocity()).mul(0.005f);
                            if (rand.nextInt(50) == 0) {
                                Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
                                ParticleSpawner.spawnShipOst(1, pos, new Vector2f(velocity).add(angletovel), 0.5f);
                            }
                            Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
                            ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), pos.x, pos.y, velocity.x + angletovel.x, velocity.y + angletovel.y, 2.0f * rand.nextFloat());
                        }
                    }
                } else if (userData instanceof CollisionObject) {
                    if (world.isRemote()) {
                        CollisionObject obj = (CollisionObject) userData;
                        Random rand = world.getRand();
                        ParticleSpawner.spawnBeamDamage(raycast, scale.x, sizeSpeed, beamColor);
                        if (rand.nextInt(5) == 0) {
                            Vector2 pos1 = raycast.getPoint();
                            Vector2f pos = new Vector2f((float) pos1.x, (float) pos1.y);
                            Vector2f velocity = new Vector2f(obj.getVelocity()).mul(0.005f);
                            if (rand.nextInt(50) == 0) {
                                Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
                                ParticleSpawner.spawnShipOst(1, pos, new Vector2f(velocity).add(angletovel), 0.5f);
                            }
                            Vector2f angletovel = RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f);
                            ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), pos.x, pos.y, velocity.x + angletovel.x, velocity.y + angletovel.y, 2.0f * rand.nextFloat());
                        }
                    }
                }
            }
        }
    }

    public float getBeamMaxRange() {
        return beamMaxRange;
    }

    @Override
    protected void createBullet() {

    }

    @Override
    protected void spawnShootParticles() {

    }

    @Override
    public void clear() {
        if (world.isRemote()) {
            particlesEffects.clear();
        }
    }

    public Vector4f getBeamColor() {
        return beamColor;
    }

    public Vector2f getCollisionPoint() {
        return collisionPoint;
    }

    public float getCurrentBeamRange() {
        return currentBeamRange;
    }
}
