package net.bfsr.collision.filter;

import net.bfsr.client.particle.Wreck;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import org.dyn4j.collision.Filter;

public class ShipFilter extends CollisionFilter {

    public ShipFilter(Object userData) {
        super(userData);
    }

    @Override
    public boolean isAllowed(Filter filter) {
        if (filter == null) return false;

        if (filter instanceof CollisionFilter) {
            Object otherData = ((CollisionFilter) filter).getUserData();
            return otherData instanceof Ship || otherData instanceof Bullet || otherData instanceof Wreck;
        }

        return false;
    }

}
