package net.bfsr.entity.wreck;

import clipper2.core.PathsD;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.event.entity.wreck.BulletDamageShipWreckEvent;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.ShipWreckSpawnData;

public class ShipWreck extends DamageableRigidBody<ShipData> {
    private final int maxLifeTime = 1200;

    public ShipWreck(float x, float y, float sin, float cos, float sizeX, float sizeY, ShipData shipData,
                     DamageMask mask, PathsD contours) {
        super(x, y, sin, cos, sizeX, sizeY, shipData, ShipRegistry.INSTANCE.getId(), mask, contours);
    }

    @Override
    protected void updateLifeTime() {
        lifeTime++;
        if (SideUtils.IS_SERVER && world.isServer()) {
            if (lifeTime >= maxLifeTime) {
                setDead();
            }
        } else {
            if (lifeTime >= 60) {
                setDead();
            }
        }
    }

    public void damage(Bullet bullet, float contactX, float contactY, float normalX, float normalY) {
        eventBus.publish(new BulletDamageShipWreckEvent(this, bullet, contactX, contactY, normalX, normalY));
    }

    @Override
    public EntityPacketSpawnData createSpawnData() {
        return new ShipWreckSpawnData(this);
    }
}