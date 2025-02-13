package net.bfsr.entity.wreck;

import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.ShipWreckSpawnData;
import net.bfsr.physics.CollisionMatrixType;
import org.locationtech.jts.geom.Polygon;

public class ShipWreck extends DamageableRigidBody {
    public ShipWreck(float x, float y, float sin, float cos, float sizeX, float sizeY, ShipData shipData, DamageMask damageMask,
                     Polygon polygon, float localOffsetX, float localOffsetY) {
        super(x, y, sin, cos, sizeX, sizeY, shipData, damageMask, polygon, localOffsetX, localOffsetY);
    }

    public ShipWreck(float x, float y, float sin, float cos, float sizeX, float sizeY, ShipData shipData, Polygon polygon,
                     float localOffsetX, float localOffsetY) {
        this(x, y, sin, cos, sizeX, sizeY, shipData, new DamageMask(shipData.getDamageMaskSize().x, shipData.getDamageMaskSize().y),
                polygon, localOffsetX, localOffsetY);
    }

    @Override
    protected void initBody() {
        super.initBody();
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.01f);
    }

    @Override
    public EntityPacketSpawnData createSpawnData() {
        return new ShipWreckSpawnData(this);
    }

    @Override
    public int getCollisionMatrixType() {
        return CollisionMatrixType.SHIP_WRECK.ordinal();
    }
}