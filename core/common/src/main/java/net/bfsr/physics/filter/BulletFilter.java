package net.bfsr.physics.filter;

import net.bfsr.entity.bullet.Bullet;
import net.bfsr.util.SideUtils;

public class BulletFilter extends CollisionFilter<Bullet> {
    public BulletFilter(Bullet bullet) {
        super(bullet, Categories.BULLET_CATEGORY, SideUtils.IS_SERVER && bullet.getWorld().isServer() ?
                Categories.SHIP_CATEGORY | Categories.WRECK_CATEGORY | Categories.BULLET_CATEGORY : 0);
    }
}