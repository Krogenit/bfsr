package net.bfsr.collision.filter;

import net.bfsr.client.particle.ParticleWreck;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import org.dyn4j.collision.Filter;

public class WreckFilter extends CollisionFilter {

    public WreckFilter(Object userData) {
        super(userData);
    }

    @Override
    public boolean isAllowed(Filter filter) {
        if (filter == null) return false;

        if (filter instanceof CollisionFilter) {
            Object otherData = ((CollisionFilter) filter).getUserData();
            return otherData instanceof Ship || otherData instanceof ParticleWreck || otherData instanceof Bullet;
        }

        return false;
    }

}
