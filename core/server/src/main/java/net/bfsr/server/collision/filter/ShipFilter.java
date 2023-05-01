package net.bfsr.server.collision.filter;

import net.bfsr.entity.ship.Ship;
import net.bfsr.physics.filter.Categories;
import net.bfsr.physics.filter.CollisionFilter;

public class ShipFilter extends CollisionFilter<Ship> {
    public ShipFilter(Ship userData) {
        super(userData, Categories.SHIP_CATEGORY, Categories.all());
    }
}