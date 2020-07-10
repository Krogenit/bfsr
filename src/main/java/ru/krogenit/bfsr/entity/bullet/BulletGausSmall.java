package ru.krogenit.bfsr.entity.bullet;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.particle.ParticleRenderer;
import ru.krogenit.bfsr.client.particle.ParticleSpawner;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.collision.filter.BulletFilter;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.world.WorldClient;
import ru.krogenit.bfsr.world.WorldServer;

public class BulletGausSmall extends Bullet {
	
	public BulletGausSmall(WorldServer world, int id, float radRot, Vector2f pos, Ship ship) {
		super(world, id, 14f, radRot, pos, new Vector2f(24, 24), ship, new Vector4f(1.0f, 1.0f, 0.5f, 1.5f), 1.56f, new BulletDamage(2.5f, 5f, 2.5f));
	}

	public BulletGausSmall(WorldClient world, int id, float radRot, Vector2f pos, Ship ship) {
		super(world, id, 14f, radRot, pos, new Vector2f(24, 24), ship, TextureRegister.smallGaus, new Vector4f(1.0f, 1.0f, 0.5f, 1.5f), 1.56f, new BulletDamage(2.5f, 5f, 2.5f));
	}

	@Override
	protected void createBody(Vector2f pos) {
		super.createBody(pos);
		Vector2[] vertices = new Vector2[4];
		vertices[0] = new Vector2(-6f, -2f);
		vertices[1] = new Vector2(6f, -2f);
		vertices[2] = new Vector2(6f, 2f);
		vertices[3] = new Vector2(-6f, 2f);
		Polygon polygon = Geometry.createPolygon(vertices);
		BodyFixture bodyFixture = new BodyFixture(polygon);
		bodyFixture.setDensity(0.00001f);
		bodyFixture.setFriction(0f);
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
	public void postPhysicsUpdate(double delta) {
		super.postPhysicsUpdate(delta);
		if(world.isRemote()) {
			float size = 60f;
			Vector2f pos = getPosition();
			ParticleSpawner.spawnLight(pos, size, new Vector4f(color.x, color.y, color.z, color.w/2f), 0.5f * 60f, false, EnumParticlePositionType.Background);
		}
	}
	
}
