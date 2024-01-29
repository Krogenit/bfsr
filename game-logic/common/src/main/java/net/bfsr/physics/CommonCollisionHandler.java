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
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.world.ContactCollisionData;

@RequiredArgsConstructor
public class CommonCollisionHandler {
    protected final EventBus eventBus;

    public void bulletRigidBody(Bullet bullet, RigidBody<?> rigidBody, BodyFixture bulletFixture, BodyFixture rigidBodyFixture,
                                float contactX, float contactY, float normalX, float normalY,
                                ContactCollisionData<Body> collision) {
        collision.getContactConstraint().setEnabled(false);
        bullet.setDead();
    }

    public void bulletShip(Bullet bullet, Ship ship, BodyFixture bulletFixture, BodyFixture shipFixture, float contactX,
                           float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
        collision.getContactConstraint().setEnabled(false);
        bullet.setLastCollidedRigidBody(ship);
        ship.setLastAttacker(bullet.getOwner());
    }

    public void bulletWreck(Bullet bullet, Wreck wreck, BodyFixture bulletFixture, BodyFixture wreckFixture,
                            float contactX, float contactY, float normalX, float normalY,
                            ContactCollisionData<Body> collision) {
        collision.getContactConstraint().setEnabled(false);
        bullet.setDead();
    }

    public void bulletShipWreck(Bullet bullet, ShipWreck wreck, BodyFixture bulletFixture, BodyFixture shipWreckFixture,
                                float contactX, float contactY, float normalX, float normalY,
                                ContactCollisionData<Body> collision) {
        collision.getContactConstraint().setEnabled(false);
        bullet.setDead();
    }

    public void shipShip(Ship ship1, Ship ship2, BodyFixture ship1Fixture, BodyFixture ship2Fixture, float contactX,
                         float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {

    }

    public void shipWreck(Ship ship, Wreck wreck, BodyFixture shipFixture, BodyFixture wreckFixture, float contactX,
                          float contactY,
                          float normalX, float normalY, ContactCollisionData<Body> collision) {

    }

    public void weaponSlotBeamShip(WeaponSlotBeam weaponSlot, Ship ship, BodyFixture fixture, float contactX,
                                   float contactY, float normalX, float normalY) {

    }

    public void weaponSlotBeamWreck(WeaponSlotBeam weaponSlotBeam, Wreck wreck, BodyFixture wreckFixture, float contactX,
                                    float contactY, float normalX, float normalY) {
        weaponSlotBeam.getWeaponSlotEventBus().publish(new BeamDamageWreckEvent(weaponSlotBeam, wreck, contactX, contactY,
                normalX, normalY));
    }
}