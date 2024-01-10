package net.bfsr.physics.filter;

import net.bfsr.entity.bullet.Bullet;

public class BulletFilter extends CollisionFilter {
    public BulletFilter(Bullet bullet) {
        super(bullet, Categories.BULLET_CATEGORY, Categories.SHIP_CATEGORY | Categories.BULLET_CATEGORY);
    }
}