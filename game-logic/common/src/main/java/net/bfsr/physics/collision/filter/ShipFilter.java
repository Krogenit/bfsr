package net.bfsr.physics.collision.filter;

import org.jbox2d.dynamics.Filter;

class ShipFilter extends Filter {
    ShipFilter(long maskBits) {
        super(Categories.SHIP_CATEGORY, maskBits);
    }
}
