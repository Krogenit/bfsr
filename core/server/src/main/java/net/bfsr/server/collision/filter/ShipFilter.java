package net.bfsr.server.collision.filter;

import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;
import net.bfsr.server.entity.ship.Ship;

public class ShipFilter extends CollisionFilter<Ship> {
    public ShipFilter(Ship userData) {
        super(userData, Categories.SHIP_CATEGORY, Categories.all());
    }
}
