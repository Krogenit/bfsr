package net.bfsr.collision.filter;

import net.bfsr.entity.bullet.BulletCommon;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.entity.wreck.WreckCommon;
import org.dyn4j.collision.Filter;

public class BulletFilter extends CollisionFilter<BulletCommon> {
    public BulletFilter(BulletCommon userData) {
        super(userData);
    }

    @Override
    public boolean isAllowed(Filter filter) {
        if (filter instanceof CollisionFilter) {
            Object otherData = ((CollisionFilter<?>) filter).getUserData();
            return otherData instanceof ShipCommon && userData.canDamageShip((ShipCommon) otherData) || otherData instanceof WreckCommon || otherData instanceof BulletCommon;
        }

        return false;
    }
}
