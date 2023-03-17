package net.bfsr.client.collision.filter;

import net.bfsr.client.entity.wreck.ShipWreckDamagable;
import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;

public class ShipWreckFilter extends CollisionFilter<ShipWreckDamagable> {
    public ShipWreckFilter(ShipWreckDamagable userData) {
        super(userData, Categories.WRECK_CATEGORY, Categories.all());
    }
}
