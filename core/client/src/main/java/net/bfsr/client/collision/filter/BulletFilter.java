package net.bfsr.client.collision.filter;

import net.bfsr.client.entity.bullet.Bullet;
import net.bfsr.collision.filter.Categories;
import net.bfsr.collision.filter.CollisionFilter;

public class BulletFilter extends CollisionFilter<Bullet> {
    public BulletFilter(Bullet userData) {
        super(userData, Categories.BULLET_CATEGORY, Categories.SHIP_CATEGORY | Categories.WRECK_CATEGORY | Categories.BULLET_CATEGORY);
    }
}
