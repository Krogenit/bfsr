package net.bfsr.physics.filter;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.RigidBody;
import net.bfsr.physics.CollisionMatrix;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.collision.Filter;

@Getter
@Setter
public class CollisionFilter extends CategoryFilter {
    protected RigidBody<?> userData;
    private final CollisionMatrix collisionMatrix;

    CollisionFilter(RigidBody<?> userData, long category, long mask) {
        super(category, mask);
        this.userData = userData;
        this.collisionMatrix = userData.getWorld().getCollisionMatrix();
    }

    @Override
    public boolean isAllowed(Filter filter) {
        CollisionFilter collisionFilter = (CollisionFilter) filter;
        return (this.category & collisionFilter.mask) > 0 && (collisionFilter.category & this.mask) > 0 &&
                collisionMatrix.canCollideWith(userData, collisionFilter.userData);
    }
}