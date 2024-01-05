package net.bfsr.physics.filter;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.GameObject;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.collision.Filter;

@Getter
@Setter
public class CollisionFilter extends CategoryFilter {
    protected GameObject userData;

    CollisionFilter(GameObject userData, long category, long mask) {
        super(category, mask);
        this.userData = userData;
    }

    @Override
    public boolean isAllowed(Filter filter) {
        CollisionFilter collisionFilter = (CollisionFilter) filter;
        return (this.category & collisionFilter.mask) > 0 && (collisionFilter.category & this.mask) > 0 &&
                userData.canCollideWith(collisionFilter.userData);
    }
}