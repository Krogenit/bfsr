package ru.krogenit.bfsr.component.weapon.small;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;
import ru.krogenit.bfsr.client.particle.ParticleSpawner;
import ru.krogenit.bfsr.client.sound.SoundRegistry;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.collision.filter.ShipFilter;
import ru.krogenit.bfsr.component.weapon.WeaponSlot;
import ru.krogenit.bfsr.entity.bullet.BulletGausSmall;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.math.RotationHelper;
import ru.krogenit.bfsr.world.WorldServer;

public class WeaponGausSmall extends WeaponSlot {

	public WeaponGausSmall(Ship ship) {
		super(ship, new SoundRegistry[] { SoundRegistry.weaponShootGaus0, SoundRegistry.weaponShootGaus1, SoundRegistry.weaponShootGaus2 }, 30f, 5f, 14f, 0.026f, new Vector2f(26, 14), TextureRegister.gaussSmall);
	}
	
	@Override
	public void createBody() {
		Vector2f scale = new Vector2f(24, 14);
		float offset = 5f;
		float width = scale.x - offset;
		float height = scale.y - offset;
		float addX = addPosition.x;
		float addY = addPosition.y;
		Vector2[] vertecies = new Vector2[] {
				new Vector2(-width * 0.5 + addX, -height * 0.5 + addY),
				new Vector2( width * 0.5 + addX, -height * 0.5 + addY),
				new Vector2( width * 0.5 + addX,  height * 0.5 + addY),
				new Vector2(-width * 0.5 + addX,  height * 0.5 + addY)	
			};
		Polygon rectangle = Geometry.createPolygon(vertecies);
		BodyFixture bodyFixture = new BodyFixture(rectangle);
		bodyFixture.setUserData(this);
		bodyFixture.setFilter(new ShipFilter(ship));
		ship.getBody().addFixture(bodyFixture);
		ship.recalculateMass();
	}

	@Override
	protected void createBullet() {
		new BulletGausSmall((WorldServer) world, world.getNextId(), rotate, position, ship);
	}

	@Override
	protected void spawnShootParticles() {
		Vector2f pos = RotationHelper.rotate(rotate, 10, 0).add(getPosition());
		ParticleSpawner.spawnWeaponShoot(TextureRegister.particleBlue3, pos, getRotation(), 80f, new Vector4f(0.8f, 1.0f, 0.5f, 0.4f));
	}
}
