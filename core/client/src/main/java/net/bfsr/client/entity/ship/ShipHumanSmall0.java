package net.bfsr.client.entity.ship;

import net.bfsr.client.collision.filter.ShipFilter;
import net.bfsr.client.component.Damage;
import net.bfsr.client.component.Shield;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.component.Engine;
import net.bfsr.component.cargo.Cargo;
import net.bfsr.component.crew.Crew;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.ShieldRegistry;
import net.bfsr.config.component.ShieldConfig;
import net.bfsr.entity.ship.ShipType;
import net.bfsr.math.Direction;
import net.bfsr.math.RotationHelper;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.util.CollisionObjectUtils;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class ShipHumanSmall0 extends Ship {
    public ShipHumanSmall0(WorldClient world, int id, float x, float y, float rotation) {
        super(world, id, x, y, rotation, 6.4f, 6.4f, 0.5f, 0.6f, 1.0f, TextureRegister.shipHumanSmall0, TextureRegister.shipHumanSmall0Damage);
        addDamage(new Damage(this, 0.8f, 0, new Vector2f(-0.5f, 1.5f), 0.08f));
        addDamage(new Damage(this, 0.6f, 0, new Vector2f(-1.8f, -0.8f), 0.08f));
        addDamage(new Damage(this, 0.4f, 1, new Vector2f(-0.5f, -1.5f), 0.055f));
        addDamage(new Damage(this, 0.2f, 2, new Vector2f(0.8f, -0.2f), 0.05f));
    }

    @Override
    public void init() {
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.5f));

        setReactor(new Reactor(30.0f, 9.0f));

        setHull(new Hull(25.0f, 0.025f));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(25.0f, 0.45f, 1.15f));
        setArmor(armor);

        ShieldConfig shieldConfig = ShieldRegistry.INSTANCE.getShield("humanSmall0");
        Shield shield = new Shield(this, shieldConfig, 0.5f, 0.6f, 1.0f, 1.0f);
        setShield(shield);
        shield.createBody();

        setWeaponsCount(2);

        setCrew(new Crew(2));

        setCargo(new Cargo(2));

        createWeaponPosition(new Vector2f(0.7f, 2.4f));
        createWeaponPosition(new Vector2f(0.7f, -2.4f));

        maxDestroyingTimer = 60;
        maxSparksTimer = 20;
    }

    @Override
    protected void createBody(float x, float y) {
        Vector2[] vertices = new Vector2[7];
        vertices[0] = new Vector2(-2.9f, 0.2f);
        vertices[1] = new Vector2(-2.9f, -0.2f);
        vertices[2] = new Vector2(-1.0f, -3.1f);
        vertices[3] = new Vector2(0.6f, -3.1f);
        vertices[4] = new Vector2(2.7f, 0.1f);
        vertices[5] = new Vector2(0.6f, 3.1f);
        vertices[6] = new Vector2(-1.0f, 3.1f);
        BodyFixture fixture = new BodyFixture(new Polygon(vertices));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        fixture.setFilter(new ShipFilter(this));
        body.addFixture(fixture);
        recalculateMass();
        body.translate(x, y);
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    @Override
    public void spawnEngineParticles(Direction dir) {
        Vector2f shipPos = getPosition();

        float rotation = getRotation();
        if (dir == Direction.FORWARD) {
            RotationHelper.rotate(rotation, -2.3f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            Vector2 shipVelocity = body.getLinearVelocity();
            ParticleSpawner.spawnEngineBack(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y,
                    (float) shipVelocity.x / 50.0f, (float) shipVelocity.y / 50.0f, rotation,
                    10.0f, 6.0F, 0.5f, 0.5f, 1.0f, 1.0f, true);
            RotationHelper.rotate(rotation, -1.7f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            ParticleSpawner.spawnLight(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, 6.0f, 0.5f, 0.5f, 1.0f, 1.0f,
                    RenderLayer.BACKGROUND_ADDITIVE);
        } else if (dir == Direction.LEFT) {
            RotationHelper.rotate(rotation, -0.5f, 3.0f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            ParticleSpawner.spawnShipEngineSmoke(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y);
        } else if (dir == Direction.RIGHT) {
            RotationHelper.rotate(rotation, -0.5f, -3.0f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            ParticleSpawner.spawnShipEngineSmoke(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y);
        } else if (dir == Direction.BACKWARD) {
            RotationHelper.rotate(rotation, 3.0f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            ParticleSpawner.spawnShipEngineSmoke(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y);
        }
    }

    @Override
    protected void createDestroyParticles() {
        ParticleSpawner.spawnDestroyShipSmall(this);
    }

    @Override
    public ShipType getType() {
        return ShipType.HUMAN_SMALL_0;
    }
}