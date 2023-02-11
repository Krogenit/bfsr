package net.bfsr.entity.ship.human;

import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.collision.filter.ShipFilter;
import net.bfsr.component.Armor;
import net.bfsr.component.ArmorPlate;
import net.bfsr.component.Engine;
import net.bfsr.component.cargo.Cargo;
import net.bfsr.component.crew.Crew;
import net.bfsr.component.damage.Damage;
import net.bfsr.component.hull.Hull;
import net.bfsr.component.reactor.Reactor;
import net.bfsr.component.shield.ShieldSmall0;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipType;
import net.bfsr.math.Direction;
import net.bfsr.math.RotationHelper;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.world.WorldClient;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class ShipHumanSmall0 extends Ship {
    public ShipHumanSmall0(WorldServer world, float x, float y, float rotation, boolean spawned) {
        super(world, x, y, rotation, 6.4f, 6.4f, 0.5f, 0.6f, 1.0f, spawned);
    }

    public ShipHumanSmall0(WorldClient world, int id, float x, float y, float rotate) {
        super(world, id, TextureRegister.shipHumanSmall0, x, y, rotate, 6.4f, 6.4f, 0.5f, 0.6f, 1.0f);
        this.textureDamage = TextureLoader.getTexture(TextureRegister.shipHumanSmall0Damage);
        addDamage(new Damage(this, 0.8f, 0, new Vector2f(-0.5f, 1.5f), 0.08f));
        addDamage(new Damage(this, 0.6f, 0, new Vector2f(-1.8f, -0.8f), 0.08f));
        addDamage(new Damage(this, 0.4f, 1, new Vector2f(-0.5f, -1.5f), 0.055f));
        addDamage(new Damage(this, 0.2f, 2, new Vector2f(0.8f, -0.2f), 0.05f));
    }

    @Override
    public void init() {
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.5f));

        setReactor(new Reactor(30.0f, 9.0f));

        setHull(new Hull(25.0f, 0.025f, this));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(25.0f, 0.45f, 1.15f));
        setArmor(armor);

        setShield(new ShieldSmall0(this, 0.5f, 0.6f, 1.0f, 1.0f, 15.0f, 0.6f, 200));

        setWeaponsCount(2);

        setCrew(new Crew(2));

        setCargo(new Cargo(2));

        createWeaponPosition(new Vector2f(0.7f, 2.4f));
        createWeaponPosition(new Vector2f(0.7f, -2.4f));

        maxDestroingTimer = 60;
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

        if (dir == Direction.FORWARD) {
            RotationHelper.rotate(getRotation(), -2.3f, 0, rotateToVector);
            Vector2 shipVelocity = body.getLinearVelocity();
            ParticleSpawner.spawnEngineBack(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y, (float) shipVelocity.x / 50.0f, (float) shipVelocity.y / 50.0f, getRotation(),
                    10.0f, 6.0F, 0.5f, 0.5f, 1.0f, 1.0f, true);
            RotationHelper.rotate(getRotation(), -1.7f, 0, rotateToVector);
            ParticleSpawner.spawnLight(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y, 6.0f, 0.5f, 0.5f, 1.0f, 1.0f, RenderLayer.BACKGROUND_ADDITIVE);
        } else if (dir == Direction.LEFT) {
            RotationHelper.rotate(getRotation(), -0.5f, 3.0f, rotateToVector);
            ParticleSpawner.spawnShipEngineSmoke(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y);
        } else if (dir == Direction.RIGHT) {
            RotationHelper.rotate(getRotation(), -0.5f, -3.0f, rotateToVector);
            ParticleSpawner.spawnShipEngineSmoke(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y);
        } else if (dir == Direction.BACKWARD) {
            RotationHelper.rotate(getRotation(), 3.0f, 0, rotateToVector);
            ParticleSpawner.spawnShipEngineSmoke(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y);
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
