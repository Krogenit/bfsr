package ru.krogenit.bfsr.component.weapon.small;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

import ru.krogenit.bfsr.client.sound.SoundRegistry;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.collision.filter.ShipFilter;
import ru.krogenit.bfsr.component.weapon.WeaponSlotBeam;
import ru.krogenit.bfsr.entity.bullet.BulletDamage;
import ru.krogenit.bfsr.entity.ship.Ship;

public class WeaponBeamSmall extends WeaponSlotBeam {
	
	public WeaponBeamSmall(Ship ship) {
		super(ship, 400f, new BulletDamage(0.075f, 0.075f, 0.15f), new Vector4f(0.8f, 0.8f, 1f, 1f), 125f, 6f, new Vector2f(14, 10), TextureRegister.beamSmall, new SoundRegistry[] { SoundRegistry.weaponShootBeam0, SoundRegistry.weaponShootBeam1, SoundRegistry.weaponShootBeam2 });
	}
	
	@Override
	public void createBody() {
		Vector2f scale = new Vector2f(24, 16);
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
}
