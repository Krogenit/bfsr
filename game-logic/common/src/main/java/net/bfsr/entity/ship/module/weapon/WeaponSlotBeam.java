package net.bfsr.entity.ship.module.weapon;

import lombok.Getter;
import net.bfsr.config.component.weapon.beam.BeamData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.entity.bullet.Bullet;
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

import java.util.function.Consumer;

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
    private final float powerAnimationSpeed = Engine.convertToDeltaTime(3.5f);
    @Getter
    private float aliveTimerInTicks;
    private final float maxAliveTimerInTicks;

    public WeaponSlotBeam(BeamData beamData) {
        super(beamData, WeaponType.BEAM);
        this.beamMaxRange = beamData.getBeamMaxRange();
        this.damage = beamData.getDamage();
        this.maxAliveTimerInTicks = beamData.getAliveTimeInTicks();
    }

    @Override
    public void init(int id, Ship ship) {
        super.init(id, ship);
        this.detectFilter = new DetectFilter<>(true, true, new BeamFilter(ship));
    }

    @Override
    public void update() {
        updatePos();

        if (aliveTimerInTicks > 0) {
            aliveTimerInTicks--;

            if (beamPower < 1.0f) {
                beamPower += powerAnimationSpeed;
                if (beamPower >= 1.0f) {
                    beamPower = 1.0f;
                }
            } else {
                beamPower = world.getRand().nextFloat() / 3.0f + 0.66f;
            }
        } else {
            if (beamPower > 0.0f) {
                beamPower -= powerAnimationSpeed;
                if (beamPower <= 0) {
                    beamPower = 0;
                }
            } else {
                if (reloadTimer > 0) reloadTimer--;
            }
        }

        if (beamPower > 0) {
            rayCast();
        }
    }

    @Override
    public void shoot(Consumer<WeaponSlot> onShotEvent) {
        reloadTimer = timeToReload;
        aliveTimerInTicks = maxAliveTimerInTicks;
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
                    ship.getFaction() == this.ship.getFaction() ? beamPower / 2.0f * Engine.getUpdateDeltaTime() :
                            beamPower * Engine.getUpdateDeltaTime(), result.getFixture(),
                    () -> eventBus.publish(new BeamDamageShipShieldEvent(this, ship, raycast, hitX, hitY)),
                    () -> eventBus.publish(new BeamDamageShipArmorEvent(this, ship, raycast, hitX, hitY)),
                    () -> eventBus.publish(new BeamDamageShipHullEvent(this, ship, raycast, hitX, hitY)));
        } else if (userData instanceof Wreck wreck) {
            if (SideUtils.IS_SERVER && world.isServer()) {
                wreck.damage(damage.getHull() * beamPower * Engine.getUpdateDeltaTime(), collisionPoint.x, collisionPoint.y,
                        (float) normal.x, (float) normal.y);
            }
            eventBus.publish(new BeamDamageWreckEvent(this, wreck, raycast, hitX, hitY));
        }
    }

    @Override
    public void createBullet(float fastForwardTime, Consumer<Bullet> syncLogic) {}
}