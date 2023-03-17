package net.bfsr.server.collision.filter;

import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;
import net.bfsr.server.entity.wreck.ShipWreckDamagable;

public class ShipWreckFilter extends CollisionFilter<ShipWreckDamagable> {
    public ShipWreckFilter(ShipWreckDamagable userData) {
        super(userData, Categories.WRECK_CATEGORY, Categories.all());
    }
}
