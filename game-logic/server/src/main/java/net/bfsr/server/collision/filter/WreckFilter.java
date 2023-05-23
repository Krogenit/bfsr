package net.bfsr.server.collision.filter;

import net.bfsr.entity.wreck.Wreck;
import net.bfsr.physics.filter.Categories;
import net.bfsr.physics.filter.CollisionFilter;

public class WreckFilter extends CollisionFilter<Wreck> {
    public WreckFilter(Wreck userData) {
        super(userData, Categories.WRECK_CATEGORY, Categories.all());
    }
}