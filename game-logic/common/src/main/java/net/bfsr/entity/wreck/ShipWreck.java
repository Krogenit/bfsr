package net.bfsr.entity.wreck;

import lombok.Getter;
import net.bfsr.config.entity.damageable.DamageableRigidBodyConfigData;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.entity.EntityTypes;
import net.bfsr.network.packet.common.entity.spawn.ShipWreckSpawnData;
import org.locationtech.jts.geom.Polygon;

@Getter
public class ShipWreck extends DamageableRigidBody {
    private final int shipId;

    public ShipWreck(float x, float y, float sin, float cos, float sizeX, float sizeY, DamageableRigidBodyConfigData configData,
                     DamageMask damageMask, Polygon polygon, float localOffsetX, float localOffsetY, int shipId) {
        super(x, y, sin, cos, sizeX, sizeY, configData, damageMask, polygon, localOffsetX, localOffsetY);
        this.shipId = shipId;
    }

    public ShipWreck(float x, float y, float sin, float cos, float sizeX, float sizeY, DamageableRigidBodyConfigData configData,
                     Polygon polygon, float localOffsetX, float localOffsetY, int shipId) {
        this(x, y, sin, cos, sizeX, sizeY, configData, new DamageMask(configData.getDamageMaskSize().x, configData.getDamageMaskSize().y),
                polygon, localOffsetX, localOffsetY, shipId);
    }

    @Override
    protected void initBody() {
        super.initBody();
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.01f);
    }

    @Override
    public ShipWreckSpawnData createSpawnData() {
        return new ShipWreckSpawnData();
    }

    @Override
    public int getEntityType() {
        return EntityTypes.SHIP_WRECK.ordinal();
    }
}