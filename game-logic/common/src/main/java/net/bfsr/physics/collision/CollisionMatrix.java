package net.bfsr.physics.collision;

import net.bfsr.engine.physics.collision.AbstractCollisionMatrix;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.physics.CollisionMatrixType;
import net.bfsr.physics.RayCastType;

public class CollisionMatrix extends AbstractCollisionMatrix {
    public CollisionMatrix(CommonCollisionHandler collisionHandler) {
        super(Math.max(CollisionMatrixType.values().length, RayCastType.values().length));

        register(CollisionMatrixType.BULLET, CollisionMatrixType.SHIP,
                (CanCollideFunction<Bullet, Ship>) (bullet, ship) -> bullet.getLastCollidedRigidBody() != ship);

        register(CollisionMatrixType.BULLET, CollisionMatrixType.RIGID_BODY,
                (CollisionListener<Bullet, RigidBody>) collisionHandler::bulletRigidBody);
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
        register(RayCastType.WEAPON_SLOT_BEAM, CollisionMatrixType.SHIP_WRECK,
                (RayCastListener<WeaponSlotBeam, ShipWreck>) collisionHandler::weaponSlotBeamShipWreck);
    }

    private void register(CollisionMatrixType type1, CollisionMatrixType type2,
                          @SuppressWarnings("rawtypes") CanCollideFunction canCollideFunction) {
        register(type1.ordinal(), type2.ordinal(), canCollideFunction);
    }

    protected void register(CollisionMatrixType type1, CollisionMatrixType type2,
                            @SuppressWarnings("rawtypes") CollisionListener collisionListener) {
        register(type1.ordinal(), type2.ordinal(), collisionListener);
    }

    private void register(RayCastType rayCastType, CollisionMatrixType collisionMatrixType,
                          @SuppressWarnings("rawtypes") RayCastListener rayCastListener) {
        register(rayCastType.ordinal(), collisionMatrixType.ordinal(), rayCastListener);
    }
}
