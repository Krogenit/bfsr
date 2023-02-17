package net.bfsr.server.entity.bullet;

import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.collision.filter.BulletFilter;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

public class BulletGausSmall extends Bullet {
    public BulletGausSmall(WorldServer world, int id, float x, float y, Ship ship) {
        super(world, id, 70.0f, x, y, 2.4f, 2.4f, ship, 1.5f, 1.56f, new BulletDamage(2.5f, 5.0f, 2.5f));
    }

    @Override
    protected void createBody(float x, float y) {
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-0.6f, -0.2f);
        vertices[1] = new Vector2(0.6f, -0.2f);
        vertices[2] = new Vector2(0.6f, 0.2f);
        vertices[3] = new Vector2(-0.6f, 0.2f);
        Polygon polygon = Geometry.createPolygon(vertices);
        BodyFixture bodyFixture = new BodyFixture(polygon);
        bodyFixture.setDensity(PhysicsUtils.BULLET_FIXTURE_DENSITY);
        bodyFixture.setFriction(0.0f);
        bodyFixture.setRestitution(1.0f);
        bodyFixture.setFilter(new BulletFilter(this));
        body.addFixture(bodyFixture);
        body.translate(x, y);
        body.setMass(MassType.FIXED_ANGULAR_VELOCITY);
        body.setUserData(this);
        body.setBullet(true);
        body.setAngularDamping(Double.MAX_VALUE);
    }
}
