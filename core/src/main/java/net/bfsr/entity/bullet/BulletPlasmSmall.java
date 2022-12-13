package net.bfsr.entity.bullet;

import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.collision.filter.BulletFilter;
import net.bfsr.entity.ship.Ship;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.world.WorldClient;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class BulletPlasmSmall extends Bullet {
    public BulletPlasmSmall(WorldServer world, int id, float radRot, Vector2f pos, Ship ship) {
        super(world, id, 75.0f, radRot, pos, new Vector2f(2.4f, 2.4f), ship, new Vector4f(0.5f, 0.5f, 1.0f, 1.5f), 1.68f, new BulletDamage(5.0f, 2.5f, 2.5f));
    }

    public BulletPlasmSmall(WorldClient world, int id, float radRot, Vector2f pos, Ship ship) {
        super(world, id, 75.0f, radRot, pos, new Vector2f(2.4f, 2.4f), ship, TextureRegister.smallPlasm, new Vector4f(0.5f, 0.5f, 1.0f, 1.5f), 1.68f, new BulletDamage(5.0f, 2.5f, 2.5f));
    }

    @Override
    protected void createBody(Vector2f pos) {
        super.createBody(pos);
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
        body.translate(pos.x, pos.y);
        body.setMass(MassType.FIXED_ANGULAR_VELOCITY);
        body.setUserData(this);
        body.setBullet(true);
        body.setAngularDamping(Double.MAX_VALUE);
    }

    @Override
    public void postPhysicsUpdate() {
        super.postPhysicsUpdate();
        if (world.isRemote()) {
            float size = 6.0f;
            Vector2f pos = getPosition();
            ParticleSpawner.spawnLight(pos, size, new Vector4f(color.x, color.y, color.z, color.w / 2.0f), 30.0f, false, EnumParticlePositionType.Background);
        }
    }
}
