package net.bfsr.server.physics;

import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.physics.CommonCollisionHandler;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.world.ContactCollisionData;

public class CollisionHandler extends CommonCollisionHandler {
    public CollisionHandler(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void bulletRigidBody(Bullet bullet, RigidBody<?> rigidBody, BodyFixture bulletFixture, BodyFixture rigidBodyFixture,
                                float contactX, float contactY, float normalX, float normalY,
                                ContactCollisionData<Body> collision) {
        super.bulletRigidBody(bullet, rigidBody, bulletFixture, rigidBodyFixture, contactX, contactY, normalX, normalY,
                collision);
        rigidBody.damage(bullet.getDamage().getHull(), contactX, contactY, normalX, normalY);
    }

    @Override
    public void bulletWreck(Bullet bullet, Wreck wreck, BodyFixture bulletFixture, BodyFixture wreckFixture, float contactX,
                            float contactY, float normalX, float normalY, ContactCollisionData<Body> collision) {
        super.bulletWreck(bullet, wreck, bulletFixture, wreckFixture, contactX, contactY, normalX, normalY, collision);
        wreck.damage(bullet.getDamage().getHull(), contactX, contactY, normalX, normalY);
    }

    @Override
    public void bulletShipWreck(Bullet bullet, ShipWreck wreck, BodyFixture bulletFixture, BodyFixture shipWreckFixture,
                                float contactX, float contactY, float normalX, float normalY,
                                ContactCollisionData<Body> collision) {
        super.bulletShipWreck(bullet, wreck, bulletFixture, shipWreckFixture, contactX, contactY, normalX, normalY, collision);
        wreck.damage(bullet, contactX, contactY, normalX, normalY);
    }

    @Override
    public void weaponSlotBeamWreck(WeaponSlotBeam weaponSlotBeam, Wreck wreck, BodyFixture wreckFixture, float contactX,
                                    float contactY, float normalX, float normalY) {
        super.weaponSlotBeamWreck(weaponSlotBeam, wreck, wreckFixture, contactX, contactY, normalX, normalY);
        wreck.damage(weaponSlotBeam.getDamage().getHull() * weaponSlotBeam.getBeamPower() *
                Engine.getUpdateDeltaTime(), contactX, contactY, normalX, normalY);
    }
}