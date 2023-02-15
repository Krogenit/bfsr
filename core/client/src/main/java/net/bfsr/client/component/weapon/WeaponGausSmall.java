package net.bfsr.client.component.weapon;

import net.bfsr.client.component.WeaponSlot;
import net.bfsr.client.entity.Ship;
import net.bfsr.client.entity.bullet.BulletGausSmall;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.world.WorldClient;
import net.bfsr.collision.filter.ShipFilter;
import net.bfsr.math.RotationHelper;
import net.bfsr.physics.PhysicsUtils;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class WeaponGausSmall extends WeaponSlot {
    public WeaponGausSmall(Ship ship) {
        super(ship, new SoundRegistry[]{SoundRegistry.weaponShootGaus0, SoundRegistry.weaponShootGaus1, SoundRegistry.weaponShootGaus2}, 30.0f, 5.0f, 70.0f, 1.56f, 2.6f, 1.4f,
                TextureRegister.gaussSmall);
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
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        ship.getBody().addFixture(bodyFixture);
        ship.recalculateMass();
    }

    @Override
    protected void createBullet() {
        new BulletGausSmall((WorldClient) world, world.getNextId(), ship.getSin(), ship.getCos(), position.x, position.y, ship);
    }

    @Override
    protected void spawnShootParticles() {
        Vector2f pos = RotationHelper.rotate(rotation, 1.0f, 0).add(getPosition());
        ParticleSpawner.spawnWeaponShoot(TextureRegister.particleBlue3, pos, getRotation(), 8.0f, 0.8f, 1.0f, 0.5f, 0.4f);
    }
}
