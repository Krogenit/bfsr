package net.bfsr.collision.filter;

import net.bfsr.client.particle.Wreck;
import net.bfsr.entity.ship.Ship;
import org.dyn4j.collision.Filter;

public class BeamFilter extends CollisionFilter<Ship> {
    public BeamFilter(Ship userData) {
        super(userData);
    }

    @Override
    public boolean isAllowed(Filter filter) {
        if (filter == null) return false;

        if (filter instanceof CollisionFilter) {
            Object otherData = ((CollisionFilter<?>) filter).getUserData();
            return otherData != userData && (otherData instanceof Ship || otherData instanceof Wreck);
        }

        return false;
    }

}
