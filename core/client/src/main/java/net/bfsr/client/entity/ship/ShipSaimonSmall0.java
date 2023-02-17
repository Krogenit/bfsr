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

public class ShipSaimonSmall0 extends Ship {
    public ShipSaimonSmall0(WorldClient world, int id, float x, float y, float rotation) {
        super(world, id, x, y, rotation, 10, 10, 1.0f, 0.6f, 0.5f, TextureRegister.shipSaimonSmall0, TextureRegister.shipSaimonSmall0Damage);
        addDamage(new Damage(this, 0.8f, 0, new Vector2f(1.0f, -0.4f), 0.08f));
        addDamage(new Damage(this, 0.6f, 0, new Vector2f(0.5f, -1.8f), 0.1f));
        addDamage(new Damage(this, 0.4f, 1, new Vector2f(0.5f, 1.5f), 0.055f));
        addDamage(new Damage(this, 0.2f, 3, new Vector2f(-1.9f, 0), 0.06f));
    }

    @Override
    public void init() {
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.6f));

        setReactor(new Reactor(30.0f, 9.75f));

        setHull(new Hull(25.0f, 0.025f));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(20.0f, 0.3f, 1.1f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(20.0f, 0.3f, 1.1f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(20.0f, 0.3f, 1.1f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(20.0f, 0.3f, 1.1f));
        setArmor(armor);

        ShieldConfig shieldConfig = ShieldRegistry.INSTANCE.getShield("saimonSmall0");
        Shield shield = new Shield(this, shieldConfig, 1.0f, 0.6f, 0.5f, 1.0f);
        setShield(shield);
        shield.createBody();

        setWeaponsCount(2);

        setCrew(new Crew(2));

        setCargo(new Cargo(2));

        createWeaponPosition(new Vector2f(1.5f, 2.3f));
        createWeaponPosition(new Vector2f(1.5f, -2.3f));

        maxDestroyingTimer = 60;
        maxSparksTimer = 20;
    }

    @Override
    protected void createBody(float x, float y) {
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-5.05f, -1.75f);
        vertices[1] = new Vector2(-3.45f, -1.75f);
        vertices[2] = new Vector2(-0.57f, -0.95f);
        vertices[3] = new Vector2(-1.5f, -0.0f);
        BodyFixture fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);

        vertices = new Vector2[4];
        vertices[0] = new Vector2(-1.5f, 0.0f);
        vertices[1] = new Vector2(-0.57f, 0.85f);
        vertices[2] = new Vector2(-3.1f, 1.65f);
        vertices[3] = new Vector2(-5.05f, 1.65f);
        fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);

        vertices = new Vector2[6];
        vertices[0] = new Vector2(-3.3f, -0.9f);
        vertices[1] = new Vector2(4.0f, -0.9f);
        vertices[2] = new Vector2(5.06f, -0.05f);
        vertices[3] = new Vector2(4.0f, 0.74f);
        vertices[4] = new Vector2(-3.3f, 0.74f);
        vertices[5] = new Vector2(-3.9f, -0.05f);
        fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);

        vertices = new Vector2[4];
        vertices[0] = new Vector2(-0.4f, -0.9f);
        vertices[1] = new Vector2(-0.9f, -3.1f);
        vertices[2] = new Vector2(1.4f, -3.1f);
        vertices[3] = new Vector2(1.4f, -0.9f);
        fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);

        vertices = new Vector2[4];
        vertices[0] = new Vector2(-0.9f, 3.05f);
        vertices[1] = new Vector2(-0.4f, 0.75f);
        vertices[2] = new Vector2(1.4f, 0.75f);
        vertices[3] = new Vector2(1.4f, 3.05f);
        fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
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

        if (dir == Direction.FORWARD) {
            RotationHelper.rotate(getRotation(), -3.3f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            Vector2 shipVelocity = body.getLinearVelocity();
            ParticleSpawner.spawnEngineBack(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y,
                    (float) shipVelocity.x / 50.0f, (float) shipVelocity.y / 50.0f,
                    getRotation(), 10.0f, 6.0F, 1.0f, 0.5f, 0.5f, 1.0f, true);
            RotationHelper.rotate(getRotation(), -3.5f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            ParticleSpawner.spawnLight(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, 6.0f, 1.0f, 0.5f, 0.5f, 1.0f,
                    RenderLayer.BACKGROUND_ADDITIVE);
        } else if (dir == Direction.LEFT) {
            RotationHelper.rotate(getRotation(), -0, 3.0f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            ParticleSpawner.spawnShipEngineSmoke(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y);
        } else if (dir == Direction.RIGHT) {
            RotationHelper.rotate(getRotation(), -0, -3.0f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            ParticleSpawner.spawnShipEngineSmoke(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y);
        } else if (dir == Direction.BACKWARD) {
            RotationHelper.rotate(getRotation(), 5.0f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            ParticleSpawner.spawnShipEngineSmoke(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y);
        }
    }

    @Override
    protected void createDestroyParticles() {
        ParticleSpawner.spawnDestroyShipSmall(this);
    }

    @Override
    public ShipType getType() {
        return ShipType.SAIMON_SMALL_0;
    }
}
