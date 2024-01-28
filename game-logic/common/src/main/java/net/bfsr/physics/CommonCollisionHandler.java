package net.bfsr.physics;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
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
import org.joml.Math;

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
        float dx = ship2.getVelocity().x - ship1.getVelocity().x;
        float dy = ship2.getVelocity().y - ship1.getVelocity().y;
        float impactPowerForOther = (float) ((Math.sqrt(dx * dx + dy * dy)) *
                (ship1.getBody().getMass().getMass() / ship2.getBody().getMass().getMass()));

        impactPowerForOther /= 400.0f;

        if (impactPowerForOther > 0.25f) {
            ship1.damageByCollision(ship2, impactPowerForOther, contactX, contactY, normalX, normalY);
            ship2.damageByCollision(ship1, impactPowerForOther, contactX, contactY, normalX, normalY);
        }
    }

    public void weaponSlotBeamShip(WeaponSlotBeam weaponSlot, Ship ship, BodyFixture fixture, float contactX,
                                   float contactY, float normalX, float normalY) {
        float beamPower = weaponSlot.getBeamPower();
        EventBus weaponSlotEventBus = weaponSlot.getWeaponSlotEventBus();
        ship.damage(weaponSlot.getDamage(), weaponSlot.getShip(), contactX, contactY,
                ship.getFaction() == weaponSlot.getShip().getFaction() ? beamPower / 2.0f * Engine.getUpdateDeltaTime() :
                        beamPower * Engine.getUpdateDeltaTime(), fixture,
                () -> weaponSlotEventBus.publish(
                        new BeamDamageShipShieldEvent(weaponSlot, ship, contactX, contactY, normalX, normalY)),
                () -> weaponSlotEventBus.publish(
                        new BeamDamageShipArmorEvent(weaponSlot, ship, contactX, contactY, normalX, normalY)),
                () -> weaponSlotEventBus.publish(
                        new BeamDamageShipHullEvent(weaponSlot, ship, contactX, contactY, normalX, normalY)));
    }

    public void weaponSlotBeamWreck(WeaponSlotBeam weaponSlotBeam, Wreck wreck, BodyFixture wreckFixture, float contactX,
                                    float contactY, float normalX, float normalY) {
        weaponSlotBeam.getWeaponSlotEventBus().publish(new BeamDamageWreckEvent(weaponSlotBeam, wreck, contactX, contactY,
                normalX, normalY));
    }
}