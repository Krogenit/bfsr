package net.bfsr.client.component.weapon;

import net.bfsr.client.collision.filter.ShipFilter;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class WeaponLaserSmall extends WeaponSlot {
    public WeaponLaserSmall(Ship ship) {
        super(ship, new SoundRegistry[]{SoundRegistry.weaponShootLaser0, SoundRegistry.weaponShootLaser1}, 30.0f, 5.0f, 75.0f, 1.68f, 2.6f, 1.4f, TextureRegister.laserSmall);
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
        ship.recalculateMass();
    }

    @Override
    protected void spawnShootParticles() {
        Vector2f pos = RotationHelper.rotate(rotation, 1.0f, 0.0f).add(getPosition());
        ParticleSpawner.spawnWeaponShoot(TextureRegister.particleBlue3, pos, getRotation(), 8.0f, 1.0f, 0.5f, 0.5f, 0.4f);
    }
}