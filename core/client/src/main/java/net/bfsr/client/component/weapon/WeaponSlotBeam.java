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
import net.bfsr.component.hull.Hull;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.config.weapon.beam.BeamData;
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

import java.util.Random;

import static net.bfsr.client.particle.effect.ParticleSpawner.CACHED_VECTOR;

public class WeaponSlotBeam extends WeaponSlot {
    @Getter
    private final float beamMaxRange;
    @Getter
    private float currentBeamRange;
    protected boolean maxPower;
    @Getter
    private final Vector2f collisionPoint = new Vector2f();
    private final BulletDamage damage;
    protected final Random rand;
    private final Vector2 rayStart = new Vector2();
    private final Ray ray = new Ray(0);
    private final DetectFilter<Body, BodyFixture> detectFilter;
    private final Vector2 rayDirection = new Vector2();
    private final Beam beam;
    private final SpawnAccumulator weaponSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator damageSpawnAccumulator = new SpawnAccumulator();

    public WeaponSlotBeam(Ship ship, BeamData beamData) {
        super(ship, beamData);
        this.beamMaxRange = beamData.getBeamMaxRange();
        this.damage = beamData.getDamage();
        this.effectsColor.w = 0.0f;
        this.rand = world.getRand();
        this.beam = new Beam(this, ship);
        this.detectFilter = new DetectFilter<>(true, true, new BeamFilter(ship));
    }

    @Override
    public void update() {
        beam.setLastValues();
        super.update();

        if (reloadTimer > 0) {
            if (reloadTimer <= timeToReload / 3.0f) {
                maxPower = false;
                if (effectsColor.w > 0.0f) {
                    effectsColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (effectsColor.w < 0) effectsColor.w = 0;
                }
            } else {
                if (!maxPower && effectsColor.w < 1.0f) {
                    effectsColor.w += 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (effectsColor.w > 1.0f) effectsColor.w = 1.0f;
                } else {
                    maxPower = true;
                }

                if (maxPower) {
                    effectsColor.w = rand.nextFloat() / 3.0f + 0.66f;
                }

                float shipRotation = ship.getRotation();
                RotationHelper.angleToVelocity(shipRotation, 10.0f, CACHED_VECTOR);
                BeamEffects.beam(position.x, position.y, 2.0f, shipRotation, CACHED_VECTOR.x, CACHED_VECTOR.y,
                        effectsColor.x, effectsColor.y, effectsColor.z, effectsColor.w, weaponSpawnAccumulator);
            }

            rayCast();

            if (effectsColor.w > 0) {
                beam.update();
            }
        } else {
            if (effectsColor.w > 0.0f) {
                effectsColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                if (effectsColor.w < 0) effectsColor.w = 0;
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
        rayStart.x = startX + position.x;
        rayStart.y = startY + position.y;
        collisionPoint.x = 0;
        collisionPoint.y = 0;
        ray.setStart(rayStart);
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
            ship.attackShip(damage, this.ship, collisionPoint, ship.getFaction() == this.ship.getFaction() ? effectsColor.w / 2.0f * 60.0f * TimeUtils.UPDATE_DELTA_TIME :
                    effectsColor.w * 60.0f * TimeUtils.UPDATE_DELTA_TIME);
            onDamageShip(raycast, hitX, hitY);
        } else if (userData instanceof Wreck wreck) {
            wreck.damage(damage.getHull() * effectsColor.w);
            onDamageWreck(raycast, hitX, hitY, wreck);
        } else if (userData instanceof CollisionObject collisionObject) {
            onDamageObject(raycast, hitX, hitY, collisionObject);
        } else {
            damageSpawnAccumulator.resetTime();
        }
    }

    protected void onDamageShip(Raycast raycast, float hitX, float hitY) {
        BeamEffects.beamDamage(raycast, hitX, hitY, scale.x, effectsColor, damageSpawnAccumulator);

        ShieldCommon shield = ship.getShield();
        if (shield == null || shield.getShield() <= 0) {
            Hull hull = ship.getHull();
            GarbageSpawner.beamHullDamage(hitX, hitY, ship.getVelocity().x * 0.005f, ship.getVelocity().y * 0.005f, () -> hull.getHull() / hull.getMaxHull() < 0.5f);
        }
    }

    protected void onDamageWreck(Raycast raycast, float hitX, float hitY, Wreck wreck) {
        BeamEffects.beamDamage(raycast, hitX, hitY, scale.x, effectsColor, damageSpawnAccumulator);
        GarbageSpawner.beamHullDamage(hitX, hitY, wreck.getVelocity().x * 0.005f, wreck.getVelocity().y * 0.005f);
    }

    protected void onDamageObject(Raycast raycast, float hitX, float hitY, CollisionObject collisionObject) {
        BeamEffects.beamDamage(raycast, hitX, hitY, scale.x, effectsColor, damageSpawnAccumulator);
        GarbageSpawner.beamHullDamage(hitX, hitY, collisionObject.getVelocity().x * 0.005f, collisionObject.getVelocity().y * 0.005f);
    }

    @Override
    protected void spawnShootParticles() {
        weaponSpawnAccumulator.resetTime();
        damageSpawnAccumulator.resetTime();
    }

    @Override
    public void renderAdditive() {
        if (reloadTimer > 0 && effectsColor.w > 0) {
            beam.render();

            if (reloadTimer > timeToReload / 3.0f) {
                SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                        lastScale.x * 2.5f, lastScale.y * 2.5f, scale.x * 2.5f, scale.y * 2.5f, effectsColor.x, effectsColor.y, effectsColor.z, 0.6f * effectsColor.w,
                        TextureLoader.getTexture(TextureRegister.particleLight), BufferType.ENTITIES_ADDITIVE);
            }
        }
    }
}