package net.bfsr.physics.filter;

import net.bfsr.engine.util.SideUtils;
import net.bfsr.entity.bullet.Bullet;

public class BulletFilter extends CollisionFilter {
    public BulletFilter(Bullet bullet) {
        super(bullet, Categories.BULLET_CATEGORY, SideUtils.IS_SERVER && bullet.getWorld().isServer() ?
                Categories.SHIP_CATEGORY | Categories.BULLET_CATEGORY : 0);
    }
}