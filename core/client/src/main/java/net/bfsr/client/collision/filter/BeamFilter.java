package net.bfsr.client.collision.filter;

import net.bfsr.client.entity.ship.Ship;
import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;

public class BeamFilter extends CollisionFilter<Ship> {
    public BeamFilter(Ship userData) {
        super(userData, Categories.BEAM_CATEGORY, Categories.SHIP_CATEGORY | Categories.WRECK_CATEGORY);
    }
}
