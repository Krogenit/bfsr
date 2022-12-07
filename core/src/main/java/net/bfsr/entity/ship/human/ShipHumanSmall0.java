package net.bfsr.entity.ship.human;

import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.texture.TextureRegister;
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
import net.bfsr.math.Direction;
import net.bfsr.math.RotationHelper;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.world.WorldClient;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShipHumanSmall0 extends Ship {
    public ShipHumanSmall0(WorldServer w, Vector2f pos, float rot, boolean spawned) {
        super(w, pos, rot, new Vector2f(6.4f, 6.4f), new Vector3f(0.5f, 0.6f, 1.0f), spawned);
    }

    public ShipHumanSmall0(WorldClient w, int id, Vector2f pos, float rot) {
        super(w, id, TextureRegister.shipHumanSmall0, pos, rot, new Vector2f(6.4f, 6.4f), new Vector3f(0.5f, 0.6f, 1.0f));
        textureDamage = TextureLoader.getTexture(TextureRegister.shipHumanSmall0Damage);
        addDamage(new Damage(this, 0.8f, 0, new Vector2f(-0.5f, 1.5f), 0.08f));
        addDamage(new Damage(this, 0.6f, 0, new Vector2f(-1.8f, -0.8f), 0.08f));
        addDamage(new Damage(this, 0.4f, 1, new Vector2f(-0.5f, -1.5f), 0.055f));
        addDamage(new Damage(this, 0.2f, 2, new Vector2f(0.8f, -0.2f), 0.05f));
    }

    @Override
    protected void init() {
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.5f));

        setReactor(new Reactor(30.0f, 9.0f));

        setHull(new Hull(25.0f, 0.025f, this));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(25.0f, 0.45f, 1.15f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(25.0f, 0.45f, 1.15f));
        setArmor(armor);

        setShield(new ShieldSmall0(this, new Vector4f(0.5f, 0.6f, 1.0f, 1.0f), 15.0f, 0.6f, 200));

        setWeaponsCount(2);

        setCrew(new Crew(2));

        setCargo(new Cargo(2));

        createWeaponPosition(new Vector2f(0.7f, 2.4f));
        createWeaponPosition(new Vector2f(0.7f, -2.4f));

        maxDestroingTimer = 60;
        maxSparksTimer = 20;
    }

    @Override
    protected void createBody(Vector2f pos) {
        super.createBody(pos);

        Vector2[] vertices = new Vector2[7];
        vertices[0] = new Vector2(-2.9f, 0.2f);
        vertices[1] = new Vector2(-2.9f, -0.2f);
        vertices[2] = new Vector2(-1.0f, -3.1f);
        vertices[3] = new Vector2(0.6f, -3.1f);
        vertices[4] = new Vector2(2.7f, 0.1f);
        vertices[5] = new Vector2(0.6f, 3.1f);
        vertices[6] = new Vector2(-1.0f, 3.1f);
        BodyFixture fixture = new BodyFixture(Geometry.createPolygon(vertices));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        fixture.setFilter(new ShipFilter(this));
        body.addFixture(fixture);
        recalculateMass();
        body.translate(pos.x, pos.y);
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    @Override
    public void spawnEngineParticles(Direction dir) {
        Vector2f shipPos = getPosition();

        switch (dir) {
            case FORWARD:
                Vector2f offset = RotationHelper.rotate(getRotation(), -2.3f, 0);
                Vector2f pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                Vector2 shipVelocity = body.getLinearVelocity();
                Vector2f velocity = new Vector2f((float) shipVelocity.x / 50.0f, (float) shipVelocity.y / 50.0f);
                ParticleSpawner.spawnEngineBack(pos, velocity, getRotation(), 10.0f, 6.0F, new Vector4f(0.5f, 0.5f, 1.0f, 1.0f), true);
                RotationHelper.rotate(getRotation(), -1.7f, 0, offset);
                pos.x = shipPos.x + offset.x;
                pos.y = shipPos.y + offset.y;
                ParticleSpawner.spawnLight(pos, 6.0f, new Vector4f(0.5f, 0.5f, 1.0f, 1.0f), EnumParticlePositionType.Background);
                break;
            case LEFT:
                offset = RotationHelper.rotate(getRotation(), -0.5f, 3.0f);
                pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                ParticleSpawner.spawnShipEngineSmoke(pos);
                break;
            case RIGHT:
                offset = RotationHelper.rotate(getRotation(), -0.5f, -3.0f);
                pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                ParticleSpawner.spawnShipEngineSmoke(pos);
                break;
            case BACKWARD:
                offset = RotationHelper.rotate(getRotation(), 3.0f, 0);
                pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                ParticleSpawner.spawnShipEngineSmoke(pos);
                break;
        }
    }

    @Override
    public TextureRegister getWreckTexture(int textureOffset) {
        return TextureRegister.values()[TextureRegister.particleWreckHumanSmall0Wreck0.ordinal() + textureOffset];
    }

    @Override
    public TextureRegister getWreckFireTexture(int textureOffset) {
        return TextureRegister.values()[TextureRegister.particleWreckHumanSmall0Fire0.ordinal() + textureOffset];
    }

    @Override
    public TextureRegister getWreckLightTexture(int textureOffset) {
        return TextureRegister.values()[TextureRegister.particleWreckHumanSmall0Light0.ordinal() + textureOffset];
    }

    @Override
    protected void createDestroyParticles() {
        ParticleSpawner.spawnDestroyShipSmall(this);
    }
}
