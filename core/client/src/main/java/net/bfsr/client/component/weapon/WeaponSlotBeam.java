package net.bfsr.client.component.weapon;

import lombok.Getter;
import net.bfsr.client.collision.filter.BeamFilter;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.client.particle.Beam;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.client.particle.effect.GarbageSpawner;
import net.bfsr.client.renderer.SpriteRenderer;
import net.bfsr.client.renderer.buffer.BufferType;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.math.RotationHelper;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.TimeUtils;
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

import static net.bfsr.client.particle.effect.ParticleSpawner.CACHED_VECTOR;

public abstract class WeaponSlotBeam extends WeaponSlot {
    private final Vector2 start = new Vector2();
    private final BeamFilter filter = new BeamFilter(null);
    @Getter
    private final float beamMaxRange;
    @Getter
    private float currentBeamRange;
    protected boolean maxColor;
    @Getter
    private final Vector2f collisionPoint = new Vector2f();
    private final BulletDamage damage;
    @Getter
    protected final Vector4f beamColor;
    protected final Random rand;
    private final Ray ray = new Ray(0);
    private final DetectFilter<Body, BodyFixture> detectFilter = new DetectFilter<>(true, true, filter);
    private final Vector2 rayDirection = new Vector2();
    private final Beam beam;
    private final SpawnAccumulator weaponSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator damageSpawnAccumulator = new SpawnAccumulator();

    protected WeaponSlotBeam(Ship ship, float beamMaxRange, BulletDamage damage, Vector4f beamColor, float shootTimerMax, float energyCost, float scaleX, float scaleY,
                             TextureRegister texture, SoundRegistry[] shootSounds) {
        super(ship, shootSounds, shootTimerMax, energyCost, Float.MAX_VALUE, 0.0f, scaleX, scaleY, texture);
        this.beamMaxRange = beamMaxRange;
        this.damage = damage;
        this.beamColor = beamColor;
        this.beamColor.w = 0.0f;
        this.rand = world.getRand();
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

                float shipRotation = ship.getRotation();
                RotationHelper.angleToVelocity(shipRotation, 10.0f, CACHED_VECTOR);
                BeamEffects.beam(position.x, position.y, 2.0f, shipRotation, CACHED_VECTOR.x, CACHED_VECTOR.y,
                        beamColor.x, beamColor.y, beamColor.z, beamColor.w, weaponSpawnAccumulator);
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
        beam.updatePosition();
    }

    public void clientShoot() {
        super.clientShoot();
        beam.init();
    }

    protected void rayCast() {
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
        if (result == null) {
            damageSpawnAccumulator.resetTime();
            currentBeamRange = beamMaxRange;
            return;
        }

        Body body = result.getBody();
        Raycast raycast = result.getRaycast();
        Vector2 point = raycast.getPoint();
        collisionPoint.x = (float) point.x;
        collisionPoint.y = (float) point.y;
        currentBeamRange = (float) raycast.getDistance();

        Object userData = body.getUserData();

        if (userData == null) {
            damageSpawnAccumulator.resetTime();
            return;
        }

        float posX = startX + cos * currentBeamRange;
        float posY = startY + sin * currentBeamRange;
        float hitX = position.x + posX;
        float hitY = position.y + posY;
        if (userData instanceof Ship ship) {
            ship.attackShip(damage, this.ship, collisionPoint, ship.getFaction() == this.ship.getFaction() ? beamColor.w / 2.0f * 60.0f * TimeUtils.UPDATE_DELTA_TIME :
                    beamColor.w * 60.0f * TimeUtils.UPDATE_DELTA_TIME);
            onDamageShip(raycast, hitX, hitY);
        } else if (userData instanceof Wreck wreck) {
            wreck.damage(damage.getHull() * beamColor.w);
            onDamageWreck(raycast, hitX, hitY, wreck);
        } else if (userData instanceof CollisionObject collisionObject) {
            onDamageObject(raycast, hitX, hitY, collisionObject);
        } else {
            damageSpawnAccumulator.resetTime();
        }
    }

    protected void onDamageShip(Raycast raycast, float hitX, float hitY) {
        BeamEffects.beamDamage(raycast, hitX, hitY, scale.x, beamColor, damageSpawnAccumulator);

        ShieldCommon shield = ship.getShield();
        if (shield == null || shield.getShield() <= 0) {
            Hull hull = ship.getHull();
            GarbageSpawner.beamHullDamage(hitX, hitY, ship.getVelocity().x * 0.005f, ship.getVelocity().y * 0.005f, () -> hull.getHull() / hull.getMaxHull() < 0.5f);
        }
    }

    protected void onDamageWreck(Raycast raycast, float hitX, float hitY, Wreck wreck) {
        BeamEffects.beamDamage(raycast, hitX, hitY, scale.x, beamColor, damageSpawnAccumulator);
        GarbageSpawner.beamHullDamage(hitX, hitY, wreck.getVelocity().x * 0.005f, wreck.getVelocity().y * 0.005f);
    }

    protected void onDamageObject(Raycast raycast, float hitX, float hitY, CollisionObject collisionObject) {
        BeamEffects.beamDamage(raycast, hitX, hitY, scale.x, beamColor, damageSpawnAccumulator);
        GarbageSpawner.beamHullDamage(hitX, hitY, collisionObject.getVelocity().x * 0.005f, collisionObject.getVelocity().y * 0.005f);
    }

    @Override
    protected void spawnShootParticles() {
        weaponSpawnAccumulator.resetTime();
        damageSpawnAccumulator.resetTime();
    }

    @Override
    public void renderAdditive() {
        if (shootTimer > 0 && beamColor.w > 0) {
            beam.render();

            if (shootTimer > shootTimerMax / 3.0f) {
                SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                        lastScale.x * 2.5f, lastScale.y * 2.5f, scale.x * 2.5f, scale.y * 2.5f, beamColor.x, beamColor.y, beamColor.z, 0.6f * beamColor.w,
                        TextureLoader.getTexture(TextureRegister.particleLight), BufferType.ENTITIES_ADDITIVE);
            }
        }
    }
}