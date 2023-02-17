package net.bfsr.client.collision.filter;

import net.bfsr.client.entity.ship.Ship;
import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;

public class ShipFilter extends CollisionFilter<Ship> {
    public ShipFilter(Ship userData) {
        super(userData, Categories.SHIP_CATEGORY, Categories.all());
    }
}
