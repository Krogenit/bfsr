package net.bfsr.entity.wreck;

import clipper2.core.PathsD;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.event.entity.wreck.BulletDamageShipWreckEvent;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.ShipWreckSpawnData;
import org.dyn4j.geometry.MassType;

public class ShipWreck extends DamageableRigidBody<ShipData> {
    public ShipWreck(float x, float y, float sin, float cos, float sizeX, float sizeY, ShipData shipData,
                     DamageMask mask, PathsD contours) {
        super(x, y, sin, cos, sizeX, sizeY, shipData, ShipRegistry.INSTANCE.getId(), mask, contours);
    }

    @Override
    protected void initBody() {
        body.setMass(MassType.NORMAL);
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.01f);
    }

    public void damage(Bullet bullet, float contactX, float contactY, float normalX, float normalY) {
        eventBus.publish(new BulletDamageShipWreckEvent(this, bullet, contactX, contactY, normalX, normalY));
    }

    @Override
    public EntityPacketSpawnData createSpawnData() {
        return new ShipWreckSpawnData(this);
    }
}