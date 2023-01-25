package net.bfsr.collision.filter;

import net.bfsr.client.particle.ParticleWreck;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import org.dyn4j.collision.Filter;

public class BulletFilter extends CollisionFilter<Bullet> {
    public BulletFilter(Bullet userData) {
        super(userData);
    }

    @Override
    public boolean isAllowed(Filter filter) {
        if (filter instanceof CollisionFilter) {
            Object otherData = ((CollisionFilter<?>) filter).getUserData();
            return otherData instanceof Ship && userData.canDamageShip((Ship) otherData) || otherData instanceof ParticleWreck || otherData instanceof Bullet;
        }

        return false;
    }
}
