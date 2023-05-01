package net.bfsr.physics.filter;

import net.bfsr.entity.wreck.ShipWreck;

public class ShipWreckFilter extends CollisionFilter<ShipWreck> {
    public ShipWreckFilter(ShipWreck userData) {
        super(userData, Categories.WRECK_CATEGORY, Categories.all());
    }
}