package net.bfsr.physics.filter;

import net.bfsr.entity.RigidBody;

public class ShipFilter extends CollisionFilter {
    public ShipFilter(RigidBody userData) {
        super(userData, Categories.SHIP_CATEGORY, Categories.all());
    }
}