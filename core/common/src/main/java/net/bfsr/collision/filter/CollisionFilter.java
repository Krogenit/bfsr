package net.bfsr.collision.filter;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.GameObject;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.collision.Filter;

@Getter
@Setter
public class CollisionFilter<T extends GameObject> extends CategoryFilter {
    protected T userData;

    protected CollisionFilter(T userData, long category, long mask) {
        super(category, mask);
        this.userData = userData;
    }

    @Override
    public boolean isAllowed(Filter filter) {
        CollisionFilter<?> collisionFilter = (CollisionFilter<?>) filter;
        return (this.category & collisionFilter.mask) > 0 && (collisionFilter.category & this.mask) > 0 && userData.canCollideWith(((CollisionFilter<?>) filter).userData);
    }
}
