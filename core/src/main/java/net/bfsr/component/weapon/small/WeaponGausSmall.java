package net.bfsr.component.weapon.small;

import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.collision.filter.ShipFilter;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.entity.bullet.BulletGausSmall;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.RotationHelper;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class WeaponGausSmall extends WeaponSlot {
    public WeaponGausSmall(Ship ship) {
        super(ship, new SoundRegistry[]{SoundRegistry.weaponShootGaus0, SoundRegistry.weaponShootGaus1, SoundRegistry.weaponShootGaus2}, 30.0f, 5.0f, 70.0f, 1.56f, new Vector2f(2.6f, 1.4f),
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
        new BulletGausSmall((WorldServer) world, world.getNextId(), rotate, position, ship);
    }

    @Override
    protected void spawnShootParticles() {
        Vector2f pos = RotationHelper.rotate(rotate, 1.0f, 0).add(getPosition());
        ParticleSpawner.spawnWeaponShoot(TextureRegister.particleBlue3, pos, getRotation(), 8.0f, new Vector4f(0.8f, 1.0f, 0.5f, 0.4f));
    }
}
