package net.bfsr.physics.filter;

import net.bfsr.entity.wreck.Wreck;

public class WreckFilter extends CollisionFilter<Wreck> {
    public WreckFilter(Wreck userData) {
        super(userData, Categories.WRECK_CATEGORY, Categories.all());
    }
}