package net.bfsr.physics;

import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.world.ContactCollisionData;

import java.util.Arrays;

public class CollisionMatrix {
    @SuppressWarnings("rawtypes")
    private final CollisionListener[][] matrix;
    @SuppressWarnings("rawtypes")
    private final RayCastListener[][] rayCastMatrix;
    @SuppressWarnings("rawtypes")
    private final CanCollideFunction[][] canCollideFunctions;

    public CollisionMatrix(CommonCollisionHandler collisionHandler) {
        int size = Math.max(CollisionMatrixType.values().length, RayCastType.values().length);
        canCollideFunctions = new CanCollideFunction[size][size];
        matrix = new CollisionListener[size][size];
        rayCastMatrix = new RayCastListener[size][size];
        CanCollideFunction<?, ?> canCollideFunction = (rigidBody1, rigidBody2) -> rigidBody1 != rigidBody2;
        CollisionListener<?, ?> listener = (rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, normalX,
                                            normalY, collision) -> {
        };
        RayCastListener<?, ?> rayCastListener = (rayCastSource, rigidBody, fixture, contactX, contactY, normalX, normalY) -> {
        };
        for (int i = 0; i < canCollideFunctions.length; i++) {
            Arrays.fill(canCollideFunctions[i], canCollideFunction);
        }
        for (int i = 0; i < matrix.length; i++) {
            Arrays.fill(matrix[i], listener);
        }
        for (int i = 0; i < rayCastMatrix.length; i++) {
            Arrays.fill(rayCastMatrix[i], rayCastListener);
        }

        register(CollisionMatrixType.BULLET, CollisionMatrixType.SHIP,
                (CanCollideFunction<Bullet, Ship>) (bullet, ship) -> bullet.getLastCollidedRigidBody() != ship);

        register(CollisionMatrixType.BULLET, CollisionMatrixType.RIGID_BODY,
                (CollisionListener<Bullet, RigidBody<?>>) collisionHandler::bulletRigidBody);
        register(CollisionMatrixType.BULLET, CollisionMatrixType.SHIP,
                (CollisionListener<Bullet, Ship>) collisionHandler::bulletShip);
        register(CollisionMatrixType.BULLET, CollisionMatrixType.WRECK,
                (CollisionListener<Bullet, Wreck>) collisionHandler::bulletWreck);
        register(CollisionMatrixType.BULLET, CollisionMatrixType.SHIP_WRECK,
                (CollisionListener<Bullet, ShipWreck>) collisionHandler::bulletShipWreck);
        register(CollisionMatrixType.SHIP, CollisionMatrixType.SHIP,
                (CollisionListener<Ship, Ship>) collisionHandler::shipShip);
        register(CollisionMatrixType.SHIP, CollisionMatrixType.WRECK,
                (CollisionListener<Ship, Wreck>) collisionHandler::shipWreck);

        register(RayCastType.WEAPON_SLOT_BEAM, CollisionMatrixType.SHIP,
                (RayCastListener<WeaponSlotBeam, Ship>) collisionHandler::weaponSlotBeamShip);
        register(RayCastType.WEAPON_SLOT_BEAM, CollisionMatrixType.WRECK,
                (RayCastListener<WeaponSlotBeam, Wreck>) collisionHandler::weaponSlotBeamWreck);
    }

    private void register(CollisionMatrixType type1, CollisionMatrixType type2,
                          @SuppressWarnings("rawtypes") CanCollideFunction canCollideFunction) {
        canCollideFunctions[type1.ordinal()][type2.ordinal()] = canCollideFunction;
        canCollideFunctions[type2.ordinal()][type1.ordinal()] = (rigidBody1, rigidBody2) -> canCollideFunction.apply(rigidBody2,
                rigidBody1);
    }

    private void register(CollisionMatrixType type1, CollisionMatrixType type2,
                          @SuppressWarnings("rawtypes") CollisionListener collisionListener) {
        matrix[type1.ordinal()][type2.ordinal()] = (rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, normalX,
                                                    normalY, collision) -> collisionListener.handle(
                rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, -normalX, -normalY, collision);
        matrix[type2.ordinal()][type1.ordinal()] = (rigidBody1, rigidBody2, fixture1, fixture2, contactX, contactY, normalX,
                                                    normalY, collision) -> collisionListener.handle(rigidBody2, rigidBody1,
                fixture2, fixture1, contactX, contactY, normalX, normalY, collision);
    }

    private void register(RayCastType rayCastType, CollisionMatrixType collisionMatrixType,
                          @SuppressWarnings("rawtypes") RayCastListener rayCastListener) {
        rayCastMatrix[rayCastType.ordinal()][collisionMatrixType.ordinal()] = rayCastListener;
    }

    void collision(RigidBody<?> rigidBody1, RigidBody<?> rigidBody2, BodyFixture fixture1, BodyFixture fixture2,
                   float contactX, float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
        matrix[rigidBody1.getCollisionMatrixType()][rigidBody2.getCollisionMatrixType()].handle(rigidBody1, rigidBody2,
                fixture1, fixture2, contactX, contactY, normalX, normalY, collision);
    }

    public void rayCast(RayCastSource rayCastSource, RigidBody<?> rigidBody, BodyFixture fixture, float contactX, float contactY,
                        float normalX, float normalY) {
        rayCastMatrix[rayCastSource.getRayCastType()][rigidBody.getCollisionMatrixType()].handle(rayCastSource, rigidBody,
                fixture, contactX, contactY, normalX, normalY);
    }

    public boolean canCollideWith(RigidBody<?> rigidBody1, RigidBody<?> rigidBody2) {
        return canCollideFunctions[rigidBody1.getCollisionMatrixType()][rigidBody2.getCollisionMatrixType()].apply(rigidBody1,
                rigidBody2);
    }

    @FunctionalInterface
    private interface CollisionListener<BODY_1 extends RigidBody<?>, BODY_2 extends RigidBody<?>> {
        void handle(BODY_1 rigidBody1, BODY_2 rigidBody2, BodyFixture fixture1, BodyFixture fixture2,
                    float contactX, float contactY, float normalX, float normalY, ContactCollisionData<Body> collision);
    }

    @FunctionalInterface
    private interface RayCastListener<RAY_CAST_SOURCE extends RayCastSource, RIGID_BODY extends RigidBody<?>> {
        void handle(RAY_CAST_SOURCE rayCastSource, RIGID_BODY rigidBody, BodyFixture fixture, float contactX, float contactY,
                    float normalX, float normalY);
    }

    @FunctionalInterface
    private interface CanCollideFunction<BODY_1 extends RigidBody<?>, BODY_2 extends RigidBody<?>> {
        boolean apply(BODY_1 rigidBody1, BODY_2 rigidBody2);
    }
}