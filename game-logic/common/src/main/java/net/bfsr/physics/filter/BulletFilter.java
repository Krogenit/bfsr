package net.bfsr.physics.filter;

import org.jbox2d.dynamics.Filter;

public class BulletFilter extends Filter {
    BulletFilter() {
        super(Categories.BULLET_CATEGORY, Categories.SHIP_CATEGORY);
    }
}