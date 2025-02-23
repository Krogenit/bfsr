package net.bfsr.physics;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.EventBus;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.module.weapon.beam.BeamDamageWreckEvent;
import org.jbox2d.dynamics.Fixture;

@RequiredArgsConstructor
public class CommonCollisionHandler {
    protected final EventBus eventBus;

    public void bulletRigidBody(Bullet bullet, RigidBody rigidBody, Fixture bulletFixture, Fixture rigidFixture,
                                float contactX, float contactY, float normalX, float normalY) {
        bullet.setDead();
    }

    public void bulletShip(Bullet bullet, Ship ship, Fixture bulletFixture, Fixture shipFixture, float contactX,
                           float contactY, float normalX, float normalY) {
        bullet.setLastCollidedRigidBody(ship);
        ship.setLastAttacker(bullet.getOwner());
    }

    public void bulletWreck(Bullet bullet, Wreck wreck, Fixture bulletFixture, Fixture wreckFixture,
                            float contactX, float contactY, float normalX, float normalY) {
        bullet.setDead();
    }

    public void bulletShipWreck(Bullet bullet, ShipWreck wreck, Fixture bulletFixture, Fixture shipWreckFixture,
                                float contactX, float contactY, float normalX, float normalY) {
        bullet.setDead();
    }

    public void shipShip(Ship ship1, Ship ship2, Fixture ship1Fixture, Fixture ship2Fixture, float contactX,
                         float contactY, float normalX, float normalY) {

    }

    public void shipWreck(Ship ship, Wreck wreck, Fixture shipFixture, Fixture wreckFixture, float contactX,
                          float contactY,
                          float normalX, float normalY) {

    }

    public void weaponSlotBeamShip(WeaponSlotBeam weaponSlot, Ship ship, Fixture fixture, float contactX,
                                   float contactY, float normalX, float normalY) {

    }

    public void weaponSlotBeamWreck(WeaponSlotBeam weaponSlotBeam, Wreck wreck, Fixture wreckFixture, float contactX,
                                    float contactY, float normalX, float normalY) {
        weaponSlotBeam.getWeaponSlotEventBus().publish(new BeamDamageWreckEvent(weaponSlotBeam, wreck, contactX, contactY,
                normalX, normalY));
    }

    public void weaponSlotBeamShipWreck(WeaponSlotBeam weaponSlot, ShipWreck wreck, Fixture fixture, float contactX, float contactY,
                                        float normalX, float normalY) {

    }
}