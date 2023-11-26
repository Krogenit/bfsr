package net.bfsr.entity.ship.module.weapon;

import lombok.Getter;
import net.bfsr.config.component.weapon.beam.BeamData;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.engine.util.TimeUtils;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.module.weapon.BeamShotEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipArmorEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipHullEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipShieldEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageWreckEvent;
import net.bfsr.physics.filter.BeamFilter;
import org.dyn4j.collision.narrowphase.Raycast;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.World;
import org.dyn4j.world.result.RaycastResult;
import org.joml.Vector2f;

public class WeaponSlotBeam extends WeaponSlot {
    @Getter
    private final float beamMaxRange;
    @Getter
    private float currentBeamRange;
    @Getter
    private float beamPower;
    @Getter
    private final Vector2f collisionPoint = new Vector2f();
    private final BulletDamage damage;
    private final Vector2 rayStart = new Vector2();
    private final Ray ray = new Ray(0);
    private DetectFilter<Body, BodyFixture> detectFilter;
    private final Vector2 rayDirection = new Vector2();

    public WeaponSlotBeam(BeamData beamData) {
        super(beamData, WeaponType.BEAM);
        this.beamMaxRange = beamData.getBeamMaxRange();
        this.damage = beamData.getDamage();
    }

    @Override
    public void init(int id, Ship ship) {
        super.init(id, ship);
        this.detectFilter = new DetectFilter<>(true, true, new BeamFilter(ship));
    }

    @Override
    public void update() {
        super.update();

        if (reloadTimer > 0) {
            if (beamPower < 1.0f) {
                beamPower += 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                if (beamPower >= 1.0f) {
                    beamPower = 1.0f;
                }
            } else {
                beamPower = world.getRand().nextFloat() / 3.0f + 0.66f;
            }
        } else {
            if (beamPower > 0.0f) {
                beamPower -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                if (beamPower < 0) beamPower = 0;
            }
        }

        if (beamPower > 0) {
            rayCast();
        }
    }

    @Override
    public void shoot() {
        reloadTimer = timeToReload;
        ship.getModules().getReactor().consume(energyCost);
        eventBus.publish(new BeamShotEvent(this));
    }

    private void rayCast() {
        World<Body> physicWorld = world.getPhysicWorld();

        float cos = ship.getCos();
        float sin = ship.getSin();
        float startRange = -size.x;

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
            currentBeamRange = beamMaxRange;
            return;
        }

        Body body = result.getBody();
        Raycast raycast = result.getRaycast();
        Vector2 point = raycast.getPoint();
        Vector2 normal = raycast.getNormal();
        collisionPoint.x = (float) point.x;
        collisionPoint.y = (float) point.y;
        currentBeamRange = (float) raycast.getDistance();

        Object userData = body.getUserData();

        if (userData == null) {
            return;
        }

        float posX = startX + cos * currentBeamRange;
        float posY = startY + sin * currentBeamRange;
        float hitX = position.x + posX;
        float hitY = position.y + posY;
        if (userData instanceof Ship ship) {
            ship.damage(damage, this.ship, collisionPoint.x, collisionPoint.y,
                    ship.getFaction() == this.ship.getFaction() ? beamPower / 2.0f * TimeUtils.UPDATE_DELTA_TIME :
                            beamPower * TimeUtils.UPDATE_DELTA_TIME, result.getFixture(),
                    () -> eventBus.publish(new BeamDamageShipShieldEvent(this, ship, raycast, hitX, hitY)),
                    () -> eventBus.publish(new BeamDamageShipArmorEvent(this, ship, raycast, hitX, hitY)),
                    () -> eventBus.publish(new BeamDamageShipHullEvent(this, ship, raycast, hitX, hitY)));
        } else if (userData instanceof Wreck wreck) {
            if (SideUtils.IS_SERVER && world.isServer()) {
                wreck.damage(damage.getHull() * beamPower * TimeUtils.UPDATE_DELTA_TIME, collisionPoint.x, collisionPoint.y,
                        (float) normal.x, (float) normal.y);
            }
            eventBus.publish(new BeamDamageWreckEvent(this, wreck, raycast, hitX, hitY));
        }
    }

    @Override
    public void createBullet() {}
}