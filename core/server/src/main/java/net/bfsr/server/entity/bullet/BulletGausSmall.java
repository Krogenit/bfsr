package net.bfsr.server.entity.bullet;

import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.server.collision.filter.BulletFilter;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

public class BulletGausSmall extends Bullet {
    public BulletGausSmall(WorldServer world, int id, float x, float y, Ship ship) {
        super(world, id, 70.0f, x, y, 2.4f, 2.4f, ship, 1.5f, 1.56f, new BulletDamage(2.5f, 5.0f, 2.5f));
    }

    @Override
    protected void initBody() {
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-0.6f, -0.2f);
        vertices[1] = new Vector2(0.6f, -0.2f);
        vertices[2] = new Vector2(0.6f, 0.2f);
        vertices[3] = new Vector2(-0.6f, 0.2f);
        BodyFixture bodyFixture = new BodyFixture(new Polygon(vertices));
        bodyFixture.setFilter(new BulletFilter(this));
        body.addFixture(bodyFixture);
        body.setUserData(this);
        body.setBullet(true);
    }
}