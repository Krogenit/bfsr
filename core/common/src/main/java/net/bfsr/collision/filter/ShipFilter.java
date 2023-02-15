package net.bfsr.collision.filter;

import net.bfsr.entity.bullet.BulletCommon;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.entity.wreck.WreckCommon;
import org.dyn4j.collision.Filter;

public class ShipFilter extends CollisionFilter<ShipCommon> {
    public ShipFilter(ShipCommon userData) {
        super(userData);
    }

    @Override
    public boolean isAllowed(Filter filter) {
        if (filter == null) return false;

        if (filter instanceof CollisionFilter) {
            Object otherData = ((CollisionFilter<?>) filter).getUserData();
            return otherData instanceof ShipCommon || otherData instanceof BulletCommon || otherData instanceof WreckCommon;
        }

        return false;
    }

}
