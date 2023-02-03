package net.bfsr.component.weapon;

import net.bfsr.client.particle.Beam;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.ParticleWreck;
import net.bfsr.client.particle.RenderLayer;
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

import java.util.Random;

public abstract class WeaponSlotBeam extends WeaponSlot {
    private final Vector2 start = new Vector2();
    private final BeamFilter filter = new BeamFilter(null);
    private final float beamMaxRange;
    private float currentBeamRange;
    private boolean maxColor;
    private final Vector2f collisionPoint = new Vector2f();
    private final BulletDamage damage;
    private final Vector4f beamColor;
    private final Random rand;
    private final Ray ray = new Ray(0);
    private final DetectFilter<Body, BodyFixture> detectFilter = new DetectFilter<>(true, true, filter);
    private final Vector2 rayDirection = new Vector2();
    private Beam beam;

    protected WeaponSlotBeam(Ship ship, float beamMaxRange, BulletDamage damage, Vector4f beamColor, float shootTimerMax, float energyCost, float scaleX, float scaleY, TextureRegister texture,
                             SoundRegistry[] shootSounds) {
        super(ship, shootSounds, shootTimerMax, energyCost, Float.MAX_VALUE, 0.0f, scaleX, scaleY, texture);
        this.beamMaxRange = beamMaxRange;
        this.damage = damage;
        this.beamColor = beamColor;
        this.beamColor.w = 0.0f;
        rand = world.getRand();

        if (world.isRemote()) {
            beam = new Beam(this, ship);
        }
    }

    @Override
    public void update() {
        if (world.isRemote()) {
            beam.setLastValues();
        }

        super.update();

        if (world.isRemote()) {
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
        } else {
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
                        beamColor.w = world.getRand().nextFloat() / 3.0f + 0.66f;
                    }
                }

                rayCast();
            } else {
                if (beamColor.w > 0.0f) {
                    beamColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (beamColor.w < 0) beamColor.w = 0;
                }
            }
        }
    }

    @Override
    public void updatePos() {
        super.updatePos();
        if (world.isRemote() && shootTimer > 0 && beamColor.w > 0) {
            beam.updatePosition();
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
        float energy = ship.getReactor().getEnergy();
        playSound();
        shootTimer = shootTimerMax;
        ship.getReactor().setEnergy(energy - energyCost);
        beam.init();
    }

    private void rayCast() {
        World<Body> physicWorld = world.getPhysicWorld();

        float cos = ship.getCos();
        float sin = ship.getSin();
        float startRange = -scale.x;

        float startX = cos * startRange;
        float startY = sin * startRange;
        start.x = startX + position.x;
        start.y = startY + position.y;
        collisionPoint.x = 0;
        collisionPoint.y = 0;
        filter.setUserData(ship);
        ray.setStart(start);
        rayDirection.set(cos, sin);
        ray.setDirection(rayDirection);
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
                float posX = startX + cos * currentBeamRange;
                float posY = startY + sin * currentBeamRange;
                float hitX = position.x + posX;
                float hitY = position.y + posY;
                if (userData instanceof Ship ship) {
                    ship.attackShip(damage, this.ship, collisionPoint, ship.getFaction() == this.ship.getFaction() ? beamColor.w / 2.0f * 60.0f * TimeUtils.UPDATE_DELTA_TIME :
                            beamColor.w * 60.0f * TimeUtils.UPDATE_DELTA_TIME);
                    if (world.isRemote()) {
                        Random rand = world.getRand();
                        ParticleSpawner.spawnBeamDamage(raycast, hitX, hitY, scale.x, sizeSpeed, beamColor);

                        Shield shield = ship.getShield();
                        if (shield == null || shield.getShield() <= 0) {
                            if (rand.nextInt(5) == 0) {
                                Hull hull = ship.getHull();
                                Vector2f velocity = new Vector2f(ship.getVelocity()).mul(0.005f);
                                if (hull.getHull() / hull.getMaxHull() < 0.5f && rand.nextInt(50) == 0) {
                                    RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                                    ParticleSpawner.spawnShipOst(1, hitX, hitY, velocity.x + ParticleSpawner.CACHED_VECTOR.x,
                                            velocity.y + ParticleSpawner.CACHED_VECTOR.y, 0.5f);
                                }
                                RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                                ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), hitX, hitY, velocity.x + ParticleSpawner.CACHED_VECTOR.x,
                                        velocity.y + ParticleSpawner.CACHED_VECTOR.y, 2.0f * rand.nextFloat());
                            }
                        }
                    }
                } else if (userData instanceof ParticleWreck wreck) {
                    wreck.damage(damage.getBulletDamageHull() * beamColor.w);
                    if (world.isRemote()) {
                        Random rand = world.getRand();
                        ParticleSpawner.spawnBeamDamage(raycast, hitX, hitY, scale.x, sizeSpeed, beamColor);
                        if (rand.nextInt(5) == 0) {
                            Vector2f velocity = new Vector2f(wreck.getVelocity()).mul(0.005f);
                            if (rand.nextInt(50) == 0) {
                                RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                                ParticleSpawner.spawnShipOst(1, hitX, hitY, velocity.x + ParticleSpawner.CACHED_VECTOR.x,
                                        velocity.y + ParticleSpawner.CACHED_VECTOR.y, 0.5f);
                            }
                            RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                            ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), hitX, hitY, velocity.x + ParticleSpawner.CACHED_VECTOR.x,
                                    velocity.y + ParticleSpawner.CACHED_VECTOR.y, 2.0f * rand.nextFloat());
                        }
                    }
                } else if (userData instanceof CollisionObject) {
                    if (world.isRemote()) {
                        CollisionObject obj = (CollisionObject) userData;
                        Random rand = world.getRand();
                        ParticleSpawner.spawnBeamDamage(raycast, hitX, hitY, scale.x, sizeSpeed, beamColor);
                        if (rand.nextInt(5) == 0) {
                            Vector2f velocity = new Vector2f(obj.getVelocity()).mul(0.005f);
                            if (rand.nextInt(50) == 0) {
                                RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                                ParticleSpawner.spawnShipOst(1, hitX, hitY, velocity.x + ParticleSpawner.CACHED_VECTOR.x, velocity.y + ParticleSpawner.CACHED_VECTOR.y, 0.5f);
                            }
                            RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 1.5f, ParticleSpawner.CACHED_VECTOR);
                            ParticleSpawner.spawnSmallGarbage(rand.nextInt(4), hitX, hitY, velocity.x + ParticleSpawner.CACHED_VECTOR.x,
                                    velocity.y + ParticleSpawner.CACHED_VECTOR.y, 2.0f * rand.nextFloat());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void renderAdditive(float interpolation) {
        if (shootTimer > 0 && beamColor.w > 0) {
//            beam.calculateTransform(this, ship);
            beam.render(null, interpolation);
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
