package net.bfsr.client.collision.filter;

import net.bfsr.client.entity.wreck.Wreck;
import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;

public class WreckFilter extends CollisionFilter<Wreck> {
    public WreckFilter(Wreck userData) {
        super(userData, Categories.WRECK_CATEGORY, Categories.all());
    }
}
