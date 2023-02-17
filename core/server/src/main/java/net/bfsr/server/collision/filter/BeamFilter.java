package net.bfsr.server.collision.filter;

import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;
import net.bfsr.server.entity.ship.Ship;

public class BeamFilter extends CollisionFilter<Ship> {
    public BeamFilter(Ship userData) {
        super(userData, Categories.BEAM_CATEGORY, Categories.SHIP_CATEGORY | Categories.WRECK_CATEGORY);
    }
}
