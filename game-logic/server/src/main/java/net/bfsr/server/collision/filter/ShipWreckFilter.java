package net.bfsr.server.collision.filter;

import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.physics.filter.Categories;
import net.bfsr.physics.filter.CollisionFilter;

public class ShipWreckFilter extends CollisionFilter<ShipWreck> {
    public ShipWreckFilter(ShipWreck userData) {
        super(userData, Categories.WRECK_CATEGORY, Categories.all());
    }
}