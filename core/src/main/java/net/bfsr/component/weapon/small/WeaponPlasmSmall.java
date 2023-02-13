package net.bfsr.component.weapon.small;

import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.collision.filter.ShipFilter;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.entity.bullet.BulletPlasmSmall;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.RotationHelper;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class WeaponPlasmSmall extends WeaponSlot {
    public WeaponPlasmSmall(Ship ship) {
        super(ship, new SoundRegistry[]{SoundRegistry.weaponShootPlasm0, SoundRegistry.weaponShootPlasm1, SoundRegistry.weaponShootPlasm2}, 30, 5, 75.0f, 1.68f, 2.6f, 1.4f,
                TextureRegister.plasmSmall);
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
        new BulletPlasmSmall((WorldServer) world, world.getNextId(), position.x, position.y, ship);
    }

    @Override
    protected void spawnShootParticles() {
        Vector2f pos = RotationHelper.rotate(rotation, 1.0f, 0).add(getPosition());
        ParticleSpawner.spawnWeaponShoot(TextureRegister.particleBlue3, pos, getRotation(), 8.0f, 0.5f, 0.5f, 1.0f, 0.4f);
    }
}
