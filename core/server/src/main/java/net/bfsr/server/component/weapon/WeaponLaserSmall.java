package net.bfsr.server.component.weapon;

import net.bfsr.server.collision.filter.ShipFilter;
import net.bfsr.server.entity.bullet.BulletLaserSmall;
import net.bfsr.server.entity.ship.Ship;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class WeaponLaserSmall extends WeaponSlot {
    public WeaponLaserSmall(Ship ship) {
        super(ship, 30.0f, 5.0f, 75.0f, 1.68f, 2.6f, 1.4f);
    }

    @Override
    public void createBody() {
        Vector2f scale = new Vector2f(2.4f, 1.4f);
        float offset = 0.5f;
        float width = scale.x - offset;
        float height = scale.y - offset;
        float addX = addPosition.x;
        float addY = addPosition.y;
        Vector2[] vertecies = {
                new Vector2(-width * 0.5 + addX, -height * 0.5 + addY),
                new Vector2(width * 0.5 + addX, -height * 0.5 + addY),
                new Vector2(width * 0.5 + addX, height * 0.5 + addY),
                new Vector2(-width * 0.5 + addX, height * 0.5 + addY)
        };
        Polygon rectangle = Geometry.createPolygon(vertecies);
        BodyFixture bodyFixture = new BodyFixture(rectangle);
        bodyFixture.setUserData(this);
        bodyFixture.setFilter(new ShipFilter(ship));
        bodyFixture.setDensity(0.0001f);
        ship.getBody().addFixture(bodyFixture);
        ship.getBody().updateMass();
    }

    @Override
    protected void createBullet() {
        new BulletLaserSmall(world, world.getNextId(), position.x, position.y, ship);
    }
}