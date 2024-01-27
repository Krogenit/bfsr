package net.bfsr.physics;

import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.bullet.BulletDamageShipArmorEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipHullEvent;
import net.bfsr.event.entity.bullet.BulletDamageShipShieldEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipArmorEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipHullEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageShipShieldEvent;
import net.bfsr.event.module.weapon.beam.BeamDamageWreckEvent;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.world.ContactCollisionData;
import org.joml.Vector2f;

import java.util.Arrays;

public class CollisionMatrix {
    @SuppressWarnings("rawtypes")
    private final CollisionListener[][] matrix;
    @SuppressWarnings("rawtypes")
    private final RayCastListener[][] rayCastMatrix;

    public CollisionMatrix(EventBus eventBus) {
        int size = Math.max(CollisionMatrixType.values().length, RayCastType.values().length);
        matrix = new CollisionListener[size][size];
        rayCastMatrix = new RayCastListener[size][size];
        CollisionListener<?, ?> listener = (rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, normalX,
                                            normalY, collision) -> {
        };
        RayCastListener<?, ?> rayCastListener = (rayCastSource, rigidBody, fixture, contactX, contactY, normalX, normalY, hitX,
                                                 hitY) -> {
        };
        for (int i = 0; i < matrix.length; i++) {
            Arrays.fill(matrix[i], listener);
        }
        for (int i = 0; i < rayCastMatrix.length; i++) {
            Arrays.fill(rayCastMatrix[i], rayCastListener);
        }

        register(CollisionMatrixType.BULLET, CollisionMatrixType.RIGID_BODY,
                (CollisionListener<Bullet, RigidBody<?>>) (bullet, rigidBody, bulletFixture, rigidBodyFixture, contactX,
                                                           contactY, normalX, normalY, collision) -> {
                    collision.getContactConstraint().setEnabled(false);
                    rigidBody.damage(bullet.getDamage().getHull(), contactX, contactY, normalX, normalY);
                    bullet.setDead();
                });
        register(CollisionMatrixType.BULLET, CollisionMatrixType.SHIP,
                (CollisionListener<Bullet, Ship>) (bullet, ship, bulletFixture, shipFixture, contactX, contactY, normalX, normalY,
                                                   collision) -> {
                    collision.getContactConstraint().setEnabled(false);

                    if (bullet.canDamageShip(ship)) {
                        bullet.setPreviousAObject(ship);
                        ship.damage(bullet.getDamage(), ship, contactX, contactY,
                                ship.getFaction() == ship.getFaction() ? 0.5f : 1.0f,
                                shipFixture, () -> {
                                    //Shield
                                    bullet.damage();
                                    bullet.reflect(normalX, normalY);
                                    eventBus.publish(new BulletDamageShipShieldEvent(bullet, ship, contactX, contactY, normalX,
                                            normalY));
                                }, () -> {
                                    //Armor
                                    bullet.setDead();
                                    eventBus.publish(new BulletDamageShipArmorEvent(bullet, ship, contactX, contactY, normalX,
                                            normalY));
                                },
                                () -> {
                                    //Hull
                                    bullet.setDead();
                                    eventBus.publish(new BulletDamageShipHullEvent(bullet, ship, contactX, contactY, normalX,
                                            normalY));
                                });
                    }
                });
        register(CollisionMatrixType.BULLET, CollisionMatrixType.WRECK,
                (CollisionListener<Bullet, Wreck>) (bullet, wreck, bulletFixture, wreckFixture, contactX, contactY, normalX,
                                                    normalY, collision) -> {
                    collision.getContactConstraint().setEnabled(false);
                    wreck.damage(bullet.getDamage().getHull(), contactX, contactY, normalX, normalY);
                    bullet.setDead();
                });
        register(CollisionMatrixType.BULLET, CollisionMatrixType.SHIP_WRECK,
                (CollisionListener<Bullet, ShipWreck>) (bullet, wreck, bulletFixture, wreckFixture, contactX, contactY,
                                                        normalX, normalY, collision) -> {
                    collision.getContactConstraint().setEnabled(false);
                    wreck.damage(bullet, contactX, contactY, normalX, normalY);
                    bullet.setDead();
                });
        register(CollisionMatrixType.SHIP, CollisionMatrixType.SHIP,
                (CollisionListener<Ship, Ship>) (ship1, ship2, ship1Fixture, ship2Fixture, contactX, contactY, normalX, normalY,
                                                 collision) -> {
                    Vector2f velocityDif = new Vector2f(ship2.getVelocity().x - ship1.getVelocity().x,
                            ship2.getVelocity().y - ship1.getVelocity().y);
                    float impactPowerForOther = (float) ((velocityDif.length()) *
                            (ship1.getBody().getMass().getMass() / ship2.getBody().getMass().getMass()));

                    impactPowerForOther /= 400.0f;

                    if (impactPowerForOther > 0.25f) {
                        ship1.damageByCollision(ship2, impactPowerForOther, contactX, contactY, normalX, normalY);
                        ship2.damageByCollision(ship1, impactPowerForOther, contactX, contactY, normalX, normalY);
                    }
                });

        register(RayCastType.WEAPON_SLOT_BEAM, CollisionMatrixType.SHIP,
                (RayCastListener<WeaponSlotBeam, Ship>) (weaponSlot, ship, fixture, contactX, contactY, normalX, normalY,
                                                         hitX, hitY) -> {
                    float beamPower = weaponSlot.getBeamPower();
                    EventBus weaponSlotEventBus = weaponSlot.getWeaponSlotEventBus();
                    ship.damage(weaponSlot.getDamage(), weaponSlot.getShip(), contactX, contactY,
                            ship.getFaction() == weaponSlot.getShip().getFaction() ?
                                    beamPower / 2.0f * Engine.getUpdateDeltaTime() :
                                    beamPower * Engine.getUpdateDeltaTime(), fixture,
                            () -> weaponSlotEventBus.publish(
                                    new BeamDamageShipShieldEvent(weaponSlot, ship, hitX, hitY, normalX, normalY)),
                            () -> weaponSlotEventBus.publish(
                                    new BeamDamageShipArmorEvent(weaponSlot, ship, hitX, hitY, normalX, normalY)),
                            () -> weaponSlotEventBus.publish(
                                    new BeamDamageShipHullEvent(weaponSlot, ship, hitX, hitY, normalX, normalY)));
                });

        register(RayCastType.WEAPON_SLOT_BEAM, CollisionMatrixType.WRECK,
                (RayCastListener<WeaponSlotBeam, Wreck>) (weaponSlotBeam, wreck, fixture, contactX, contactY, normalX, normalY,
                                                          hitX, hitY) -> {
                    if (SideUtils.IS_SERVER && wreck.getWorld().isServer()) {
                        wreck.damage(weaponSlotBeam.getDamage().getHull() * weaponSlotBeam.getBeamPower() *
                                Engine.getUpdateDeltaTime(), contactX, contactY, normalX, normalY);
                    }
                    weaponSlotBeam.getWeaponSlotEventBus().publish(new BeamDamageWreckEvent(weaponSlotBeam, wreck, hitX, hitY,
                            normalX, normalY));
                });
    }

    private void register(CollisionMatrixType type1, CollisionMatrixType type2,
                          @SuppressWarnings("rawtypes") CollisionListener collisionListener) {
        matrix[type1.ordinal()][type2.ordinal()] = (rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, normalX,
                                                    normalY, collision) -> collisionListener.compute(
                rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, -normalX, -normalY, collision);
        matrix[type2.ordinal()][type1.ordinal()] = (rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, normalX,
                                                    normalY, collision) -> collisionListener.compute(rigidBody2, rigidBody1,
                fixture2, fixture1, contactX, contactY, normalX, normalY, collision);
    }

    private void register(RayCastType rayCastType, CollisionMatrixType collisionMatrixType,
                          @SuppressWarnings("rawtypes") RayCastListener rayCastListener) {
        rayCastMatrix[rayCastType.ordinal()][collisionMatrixType.ordinal()] = rayCastListener;
    }

    void collision(RigidBody<?> rigidBody1, RigidBody<?> rigidBody2, BodyFixture fixture1, BodyFixture fixture2,
                   float contactX, float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
        matrix[rigidBody1.getCollisionMatrixType()][rigidBody2.getCollisionMatrixType()].compute(rigidBody1, rigidBody2,
                fixture1, fixture2, contactX, contactY, normalX, normalY, collision);
    }

    public void rayCast(RayCastSource rayCastSource, RigidBody<?> rigidBody, BodyFixture fixture, float contactX, float contactY,
                        float normalX, float normalY, float hitX, float hitY) {
        rayCastMatrix[rayCastSource.getRayCastType()][rigidBody.getCollisionMatrixType()].compute(rayCastSource, rigidBody,
                fixture, contactX, contactY, normalX, normalY, hitX, hitY);
    }

    @FunctionalInterface
    private interface CollisionListener<BODY_1 extends RigidBody<?>, BODY_2 extends RigidBody<?>> {
        void compute(BODY_1 rigidBody1, BODY_2 rigidBody2, BodyFixture fixture1, BodyFixture fixture2,
                     float contactX, float contactY, float normalX, float normalY, ContactCollisionData<Body> collision);
    }

    @FunctionalInterface
    private interface RayCastListener<RAY_CAST_SOURCE extends RayCastSource, RIGID_BODY extends RigidBody<?>> {
        void compute(RAY_CAST_SOURCE rayCastSource, RIGID_BODY rigidBody, BodyFixture fixture, float contactX, float contactY,
                     float normalX, float normalY, float hitX, float hitY);
    }
}