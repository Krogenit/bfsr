package net.bfsr.server.collision.filter;

import net.bfsr.entity.ship.Ship;
import net.bfsr.physics.filter.Categories;
import net.bfsr.physics.filter.CollisionFilter;

public class BeamFilter extends CollisionFilter<Ship> {
    public BeamFilter(Ship userData) {
        super(userData, Categories.BEAM_CATEGORY, Categories.SHIP_CATEGORY | Categories.WRECK_CATEGORY);
    }
}