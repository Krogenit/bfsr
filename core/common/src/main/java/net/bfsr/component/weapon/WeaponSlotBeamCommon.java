package net.bfsr.component.weapon;

import lombok.Getter;
import net.bfsr.collision.filter.BeamFilter;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.entity.wreck.WreckCommon;
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

public abstract class WeaponSlotBeamCommon extends WeaponSlotCommon {
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

    protected WeaponSlotBeamCommon(ShipCommon ship, float beamMaxRange, BulletDamage damage, Vector4f beamColor, float shootTimerMax, float energyCost, float scaleX, float scaleY) {
        super(ship, shootTimerMax, energyCost, Float.MAX_VALUE, 0.0f, scaleX, scaleY);
        this.beamMaxRange = beamMaxRange;
        this.damage = damage;
        this.beamColor = beamColor;
        this.beamColor.w = 0.0f;
        this.rand = world.getRand();
    }

    public void tryShoot() {
        float energy = ship.getReactor().getEnergy();
        if (shootTimer <= 0 && energy >= energyCost) {
            shoot();
        }
    }

    protected abstract void shoot();

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
                if (userData instanceof ShipCommon ship) {
                    ship.attackShip(damage, this.ship, collisionPoint, ship.getFaction() == this.ship.getFaction() ? beamColor.w / 2.0f * 60.0f * TimeUtils.UPDATE_DELTA_TIME :
                            beamColor.w * 60.0f * TimeUtils.UPDATE_DELTA_TIME);
                    onDamageShip(raycast, hitX, hitY, sizeSpeed);
                } else if (userData instanceof WreckCommon wreck) {
                    wreck.damage(damage.getBulletDamageHull() * beamColor.w);
                    onDamageWreck(raycast, hitX, hitY, sizeSpeed, wreck);
                } else if (userData instanceof CollisionObject collisionObject) {
                    onDamageObject(raycast, hitX, hitY, sizeSpeed, collisionObject);
                }
            }
        }
    }

    protected void onDamageObject(Raycast raycast, float hitX, float hitY, float sizeSpeed, CollisionObject collisionObject) {

    }

    protected void onDamageWreck(Raycast raycast, float hitX, float hitY, float sizeSpeed, WreckCommon wreck) {

    }

    protected void onDamageShip(Raycast raycast, float hitX, float hitY, float sizeSpeed) {

    }

    @Override
    protected void createBullet() {

    }

    @Override
    protected void spawnShootParticles() {

    }
}
