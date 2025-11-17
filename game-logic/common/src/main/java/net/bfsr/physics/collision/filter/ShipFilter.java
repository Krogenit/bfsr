package net.bfsr.physics.collision.filter;

import org.jbox2d.dynamics.Filter;

public class ShipFilter extends Filter {
    ShipFilter() {
        super(Categories.SHIP_CATEGORY, Categories.all());
    }
}