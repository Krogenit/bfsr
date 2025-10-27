package net.bfsr.physics.collision;

import net.bfsr.engine.physics.collision.AbstractCollisionMatrix;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.EntityTypes;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.physics.RayCastType;

public class CollisionMatrix extends AbstractCollisionMatrix {
    public CollisionMatrix(CommonCollisionHandler collisionHandler) {
        super(Math.max(EntityTypes.values().length, RayCastType.values().length));

        register(EntityTypes.BULLET, EntityTypes.SHIP,
                (CanCollideFunction<Bullet, Ship>) (bullet, ship) -> bullet.getLastCollidedRigidBody() != ship);

        register(EntityTypes.BULLET, EntityTypes.RIGID_BODY,
                (CollisionListener<Bullet, RigidBody>) collisionHandler::bulletRigidBody);
        register(EntityTypes.BULLET, EntityTypes.SHIP,
                (CollisionListener<Bullet, Ship>) collisionHandler::bulletShip);
        register(EntityTypes.BULLET, EntityTypes.WRECK,
                (CollisionListener<Bullet, Wreck>) collisionHandler::bulletWreck);
        register(EntityTypes.BULLET, EntityTypes.SHIP_WRECK,
                (CollisionListener<Bullet, ShipWreck>) collisionHandler::bulletShipWreck);
        register(EntityTypes.SHIP, EntityTypes.SHIP,
                (CollisionListener<Ship, Ship>) collisionHandler::shipShip);
        register(EntityTypes.SHIP, EntityTypes.WRECK,
                (CollisionListener<Ship, Wreck>) collisionHandler::shipWreck);

        register(RayCastType.WEAPON_SLOT_BEAM, EntityTypes.SHIP,
                (RayCastListener<WeaponSlotBeam, Ship>) collisionHandler::weaponSlotBeamShip);
        register(RayCastType.WEAPON_SLOT_BEAM, EntityTypes.WRECK,
                (RayCastListener<WeaponSlotBeam, Wreck>) collisionHandler::weaponSlotBeamWreck);
        register(RayCastType.WEAPON_SLOT_BEAM, EntityTypes.SHIP_WRECK,
                (RayCastListener<WeaponSlotBeam, ShipWreck>) collisionHandler::weaponSlotBeamShipWreck);
    }

    private void register(EntityTypes type1, EntityTypes type2,
                          @SuppressWarnings("rawtypes") CanCollideFunction canCollideFunction) {
        register(type1.ordinal(), type2.ordinal(), canCollideFunction);
    }

    protected void register(EntityTypes type1, EntityTypes type2,
                            @SuppressWarnings("rawtypes") CollisionListener collisionListener) {
        register(type1.ordinal(), type2.ordinal(), collisionListener);
    }

    private void register(RayCastType rayCastType, EntityTypes EntityTypes,
                          @SuppressWarnings("rawtypes") RayCastListener rayCastListener) {
        register(rayCastType.ordinal(), EntityTypes.ordinal(), rayCastListener);
    }
}
