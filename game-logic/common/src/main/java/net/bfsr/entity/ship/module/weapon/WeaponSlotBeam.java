package net.bfsr.entity.ship.module.weapon;

import lombok.Getter;
import net.bfsr.config.component.weapon.beam.BeamData;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.damage.ConnectedObjectType;
import net.bfsr.engine.Engine;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.physics.RayCastSource;
import net.bfsr.physics.RayCastType;
import net.bfsr.physics.filter.BeamFilter;
import org.dyn4j.collision.narrowphase.Raycast;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.result.RaycastResult;
import org.joml.Vector2f;

import java.util.function.Consumer;

public class WeaponSlotBeam extends WeaponSlot implements RayCastSource {
    @Getter
    private final float beamMaxRange;
    @Getter
    private float currentBeamRange;
    @Getter
    private float beamPower;
    @Getter
    private final Vector2f collisionPoint = new Vector2f();
    @Getter
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
    }

    @Override
    public void postPhysicsUpdate(RigidBody<?> rigidBody) {
        updatePos(rigidBody);

        if (beamPower > 0) {
            rayCast();
        }
    }

    @Override
    public void shoot(Consumer<WeaponSlot> onShotConsumer, Reactor reactor) {
        super.shoot(onShotConsumer, reactor);
        aliveTimerInTicks = maxAliveTimerInTicks;
    }

    private void rayCast() {
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
        RaycastResult<Body, BodyFixture> result = world.getPhysicWorld().raycastClosest(ray, beamMaxRange, detectFilter);
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

        RigidBody<?> rigidBody = (RigidBody<?>) body.getUserData();

        world.getCollisionMatrix().rayCast(this, rigidBody, result.getFixture(), collisionPoint.x, collisionPoint.y,
                (float) normal.x, (float) normal.y, position.x + startX + cos * currentBeamRange,
                position.y + startY + sin * currentBeamRange);
    }

    @Override
    public void createBullet(float fastForwardTime) {}

    @Override
    public int getRegistryId() {
        return BeamRegistry.INSTANCE.getId();
    }

    @Override
    public ConnectedObjectType getConnectedObjectType() {
        return ConnectedObjectType.WEAPON_SLOT_BEAM;
    }

    @Override
    public int getRayCastType() {
        return RayCastType.WEAPON_SLOT_BEAM.ordinal();
    }
}