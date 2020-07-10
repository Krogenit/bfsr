package ru.krogenit.bfsr.component.shield;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.particle.ParticleSpawner;
import ru.krogenit.bfsr.client.render.OpenGLHelper;
import ru.krogenit.bfsr.client.shader.BaseShader;
import ru.krogenit.bfsr.client.sound.SoundRegistry;
import ru.krogenit.bfsr.client.sound.SoundSourceEffect;
import ru.krogenit.bfsr.collision.filter.ShipFilter;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.packet.server.PacketShieldRebuild;
import ru.krogenit.bfsr.network.packet.server.PacketShieldRebuildingTime;
import ru.krogenit.bfsr.network.packet.server.PacketShieldRemove;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

import java.util.List;

public abstract class Shield extends CollisionObject {

	private float shield, maxShield;
	private float shieldRegen;
	private Vector2f radius;
	private Vector2f diameter;
	private float timeToRebuild, rebuildingTime;
	private float size;
	private final Ship ship;

	public Shield(Ship ship) {
		this.world = ship.getWorld();
		this.ship = ship;
		this.size = 1.0f;
	}

	@Override
	protected void createBody(Vector2f pos) {
		super.createBody(pos);
		
		List<BodyFixture> shipFixtures = ship.getBody().getFixtures();
		for (int i = 0; i < shipFixtures.size(); i++) {
			BodyFixture fixture = shipFixtures.get(i);
			Object userData = fixture.getUserData();
			if (userData instanceof Shield) {
				ship.getBody().removeFixture(fixture);
				i--;
			}
		}

		this.radius = new Vector2f();
		for (BodyFixture bodyFixture : ship.getBody().getFixtures()) {
			Convex convex = bodyFixture.getShape();
			if(convex instanceof Polygon) {
				Polygon polygon = (Polygon) convex;
				for(Vector2 vertex : polygon.getVertices()) {
					float x = (float) Math.abs(vertex.x);
					if (x > radius.x) {
						radius.x = x;
					}
					float y = (float) Math.abs(vertex.y);
					if (y > radius.y) {
						radius.y = y;
					}
				}
			}

		}

		float offset = 14f;
		this.diameter = new Vector2f(radius.x * 2f + offset, radius.y * 2f + offset);

//		double angleStep = 30f;
//		double radianStep = Math.toRadians(angleStep);
//		int size = (int) (360 / angleStep);
//		Vector2[] points = new Vector2[size];
//		float angle = 0f;
//		for (int i = 0; i < size; i++) {
//			points[i] = new Vector2(radius, 0);
//			MathRotationHelper.rotateAboutZ(points[i], angle);
//
//			angle += radianStep;
//		}

//		Polygon polygon = Geometry.createPolygon(points);
		Polygon ellipse = Geometry.createPolygonalEllipse(16, diameter.x, diameter.y);
		BodyFixture bodyFixture = new BodyFixture(ellipse);
		bodyFixture.setUserData(this);
		bodyFixture.setDensity(0.0001f);
		bodyFixture.setFriction(0f);
		bodyFixture.setRestitution(0.1f);
		bodyFixture.setFilter(new ShipFilter(ship));
		ship.getBody().addFixture(bodyFixture);
		ship.recalculateMass();
		this.diameter.x += 1f;
		this.diameter.y += 1f;
	}

	@Override
	public void update(double delta) {
		if (shield < maxShield && shieldAlive()) {
			shield += shieldRegen * delta;

			if (world.isRemote() && size < 1f) {
				size += 3.6f * delta;
				if (size > 1f) size = 1f;
			}

			if (shield > maxShield) {
				shield = maxShield;
			}
		}

		if (!world.isRemote() && rebuildingTime < timeToRebuild) {
			rebuildingTime += 60f * delta;

			if (rebuildingTime >= timeToRebuild) {
				rebuildShield();
			}
		}
	}

	public boolean shieldAlive() {
		return rebuildingTime >= timeToRebuild;
	}

	public void rebuildShield() {
		shield = maxShield / 5f;
		rebuildingTime = timeToRebuild;

		if (world.isRemote()) {
			Vector3f shipEffectColor = ship.getEffectsColor();
			Vector4f color = new Vector4f(shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f);
			ParticleSpawner.spawnLight(getPosition(), ship.getScale().x * 2f, color, 0.04f * 60f, false, EnumParticlePositionType.Default);
			if (ship.getWorld().getRand().nextInt(2) == 0) {
				Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp0, getPosition()));
			} else {
				Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp1, getPosition()));
			}
		} else {
			MainServer.getServer().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRebuild(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
		}

		createBody(ship.getPosition());
	}

	public boolean damage(float shieldDamage) {
		if (shield > 0) {
			shield -= shieldDamage;

			if (!world.isRemote() && shield <= 0) {
				removeShield();
			}

			return true;
		} else {
			if (!world.isRemote()) {
				setRebuildingTime(0);
				MainServer.getServer().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRebuildingTime(ship.getId(), 0), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
			}
		}

		return false;
	}

	public void setRebuildingTime(int time) {
		rebuildingTime = time;
	}

	public void removeShield() {
		List<BodyFixture> shipFixtures = ship.getBody().getFixtures();
		for (int i = 0; i < shipFixtures.size(); i++) {
			BodyFixture fixture = shipFixtures.get(i);
			Object userData = fixture.getUserData();
			if (userData instanceof Shield) {
				ship.getBody().removeFixture(fixture);
				i--;
			}
		}
		
		ship.recalculateMass();

		if (world.isRemote()) {
			Vector3f shipEffectColor = ship.getEffectsColor();
			Vector4f color = new Vector4f(shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f);
			ParticleSpawner.spawnLight(getPosition(), ship.getScale().x * 2f, 5f * 60f, color, 0.04f * 60f, false, EnumParticlePositionType.Default);
			ParticleSpawner.spawnDisableShield(getPosition(), ship.getScale().x * 4f, -240f, new Vector4f(color));
			Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldDown, ship.getPosition()));
		} else {
			MainServer.getServer().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRemove(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
		}

		rebuildingTime = 0;
		size = 0f;
		shield = 0;
	}

	@Override
	public void render(BaseShader shader) {
		if (shieldAlive()) {
			OpenGLHelper.alphaGreater(0.01f);
			super.render(shader);
		}
	}
	
	@Override
	public float getRotation() {
		return ship.getRotation();
	}

	@Override
	public Vector2f getPosition() {
		return ship.getPosition();
	}

	public void setShield(float shield) {
		this.shield = shield;
	}

	public void setMaxShield(float maxShield) {
		this.maxShield = maxShield;
	}

	public void setTimeToRebuild(float timeToRebuild) {
		this.rebuildingTime = timeToRebuild;
		this.timeToRebuild = timeToRebuild;
	}

	public void setShieldRegen(float shieldRegen) {
		this.shieldRegen = shieldRegen;
	}

	public Ship getShip() {
		return ship;
	}

	@Override
	public Vector2f getScale() {
		return new Vector2f(diameter.x * size, diameter.y * size);
	}

	public float getShield() {
		return shield;
	}

	public float getMaxShield() {
		return maxShield;
	}

	public float getSize() {
		return size;
	}
}
