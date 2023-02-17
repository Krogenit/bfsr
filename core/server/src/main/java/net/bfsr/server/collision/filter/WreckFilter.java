package net.bfsr.server.collision.filter;

import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;
import net.bfsr.server.entity.wreck.Wreck;

public class WreckFilter extends CollisionFilter<Wreck> {
    public WreckFilter(Wreck userData) {
        super(userData, Categories.WRECK_CATEGORY, Categories.all());
    }
}
