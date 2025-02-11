package net.bfsr.entity.ship.module.weapon;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.Getter;
import net.bfsr.config.component.weapon.beam.BeamData;
import net.bfsr.damage.ConnectedObjectType;
import net.bfsr.engine.Engine;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.physics.RayCastSource;
import net.bfsr.physics.RayCastType;
import net.bfsr.physics.filter.Filters;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;
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
    private final Vector2 rayDirection = new Vector2();
    private Fixture rayCastResultFixture;
    private Vector2 rayCastResultNormal;

    private final float powerAnimationSpeed = Engine.convertToDeltaTime(3.5f);
    @Getter
    private float aliveTimerInTicks;
    private final float maxAliveTimerInTicks;
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();

    public WeaponSlotBeam(BeamData beamData) {
        super(beamData, WeaponType.BEAM);
        this.beamMaxRange = beamData.getBeamMaxRange();
        this.damage = beamData.getDamage();
        this.maxAliveTimerInTicks = beamData.getAliveTimeInTicks();
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
                beamPower = random.nextFloat() / 3.0f + 0.66f;
            }
        } else {
            if (beamPower > 0.0f) {
                beamPower -= powerAnimationSpeed;
                if (beamPower <= 0) {
                    beamPower = 0;
                    currentBeamRange = 0;
                }
            } else {
                if (reloadTimer > 0) reloadTimer--;
            }
        }
    }

    @Override
    public void postPhysicsUpdate(RigidBody rigidBody) {
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
        if (currentBeamRange < beamMaxRange) {
            currentBeamRange += 1.0f;
        }

        float cos = ship.getCos();
        float sin = ship.getSin();
        float startRange = -getSizeX();

        float startX = cos * startRange;
        float startY = sin * startRange;
        rayStart.x = startX + getX();
        rayStart.y = startY + getY();
        collisionPoint.x = 0;
        collisionPoint.y = 0;
        rayDirection.set(rayStart.x + cos * currentBeamRange, rayStart.y + sin * currentBeamRange);
        rayCastResultFixture = null;

        world.getPhysicWorld().raycast((fixture, point, normal, fraction) -> {
            if (!world.getContactFilter().shouldCollide(fixture.getFilter(), Filters.BEAM_FILTER)) {
                return -1.0f;
            }

            if (fixture.getBody() == ship.getBody()) {
                return -1.0f;
            }

            collisionPoint.x = point.x;
            collisionPoint.y = point.y;
            currentBeamRange = collisionPoint.distance(rayStart.x, rayStart.y);
            rayCastResultFixture = fixture;
            rayCastResultNormal = normal;
            return fraction;
        }, rayStart, rayDirection);

        if (rayCastResultFixture != null) {
            world.getCollisionMatrix().rayCast(this, rayCastResultFixture, collisionPoint.x, collisionPoint.y, rayCastResultNormal.x,
                    rayCastResultNormal.y);
        }
    }

    @Override
    public void createBullet(float fastForwardTime) {}

    @Override
    public ConnectedObjectType getConnectedObjectType() {
        return ConnectedObjectType.WEAPON_SLOT_BEAM;
    }

    @Override
    public int getRayCastType() {
        return RayCastType.WEAPON_SLOT_BEAM.ordinal();
    }
}