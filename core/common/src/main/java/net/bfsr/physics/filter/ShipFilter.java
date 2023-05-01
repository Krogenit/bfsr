package net.bfsr.physics.filter;

import net.bfsr.entity.ship.Ship;

public class ShipFilter extends CollisionFilter<Ship> {
    public ShipFilter(Ship userData) {
        super(userData, Categories.SHIP_CATEGORY, Categories.all());
    }
}