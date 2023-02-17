package net.bfsr.server.collision.filter;

import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;
import net.bfsr.server.entity.bullet.Bullet;

public class BulletFilter extends CollisionFilter<Bullet> {
    public BulletFilter(Bullet userData) {
        super(userData, Categories.BULLET_CATEGORY, Categories.SHIP_CATEGORY | Categories.WRECK_CATEGORY | Categories.BULLET_CATEGORY);
    }
}
