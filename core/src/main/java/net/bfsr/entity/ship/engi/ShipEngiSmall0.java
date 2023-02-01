package net.bfsr.entity.ship.engi;

import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
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

public class ShipEngiSmall0 extends Ship {
    public ShipEngiSmall0(WorldServer w, float x, float y, float rot, boolean spawned) {
        super(w, x, y, rot, 7.5f, 7.5f, 0.8f, 1.0f, 0.5f, spawned);
    }

    public ShipEngiSmall0(WorldClient w, int id, float x, float y, float rot) {
        super(w, id, TextureRegister.shipEngiSmall0, x, y, rot, 7.5f, 7.5f, 0.8f, 1.0f, 0.5f);
        this.textureDamage = TextureLoader.getTexture(TextureRegister.shipEngiSmall0Damage);
        addDamage(new Damage(this, 0.8f, 0, new Vector2f(-1.0f, 1.5f), 0.08f));
        addDamage(new Damage(this, 0.6f, 0, new Vector2f(0.5f, -1.2f), 0.08f));
        addDamage(new Damage(this, 0.4f, 1, new Vector2f(-1.5f, -0), 0.055f));
        addDamage(new Damage(this, 0.2f, 2, new Vector2f(1.2f, 0.5f), 0.05f));
    }

    @Override
    public void init() {
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.5f));
        setReactor(new Reactor(30.0f, 9.0f));
        setHull(new Hull(22.5f, 0.0225f, this));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(17.5f, 0.25f, 1.05f));
        setArmor(armor);
        setShield(new ShieldSmall0(this, 0.8f, 1.0f, 0.5f, 1.0f, 12.5f, 0.55f, 225.0f));
        setWeaponsCount(2);
        setCrew(new Crew(2));
        setCargo(new Cargo(2));
        createWeaponPosition(new Vector2f(1.8f, 1.8f));
        createWeaponPosition(new Vector2f(1.8f, -1.8f));

        maxDestroingTimer = 60;
        maxSparksTimer = 20;
    }

    @Override
    protected void createBody(float x, float y) {
        super.createBody(x, y);

        Vector2[] vertices = new Vector2[7];
        vertices[0] = new Vector2(-3.6f, 0.0f);
        vertices[1] = new Vector2(-1.7f, -2.0f);
        vertices[2] = new Vector2(1.0f, -2.0f);
        vertices[3] = new Vector2(3.6f, -0.55f);
        vertices[4] = new Vector2(3.6f, 0.55f);
        vertices[5] = new Vector2(1.0f, 2.0f);
        vertices[6] = new Vector2(-1.7f, 2.0f);
        BodyFixture fixture = new BodyFixture(Geometry.createPolygon(vertices));
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

        switch (dir) {
            case FORWARD:
                RotationHelper.rotate(getRotation(), -3.7f, 0, rotateToVector);
                Vector2 shipVelocity = body.getLinearVelocity();
                float velocityX = (float) shipVelocity.x / 50.0f;
                float velocityY = (float) shipVelocity.y / 50.0f;
                ParticleSpawner.spawnEngineBack(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y, velocityX, velocityY, getRotation(), 10.0f, 6.0F, 0.8f, 1.0f, 0.5f, 1.0f, true);
                RotationHelper.rotate(getRotation(), -3.0f, 1.1f, rotateToVector);
                ParticleSpawner.spawnEngineBack(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y, velocityX, velocityY, getRotation(), 10.0f, 6.0F, 0.8f, 1.0f, 0.5f, 1.0f, false);
                RotationHelper.rotate(getRotation(), -3.0f, -1.1f, rotateToVector);
                ParticleSpawner.spawnEngineBack(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y, velocityX, velocityY, getRotation(), 10.0f, 6.0F, 0.8f, 1.0f, 0.5f, 1.0f, false);
                RotationHelper.rotate(getRotation(), -2.7f, 0, rotateToVector);
                ParticleSpawner.spawnLight(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y, 6.0f, 0.8f, 1.0f, 0.5f, 1.0f, EnumParticlePositionType.BACKGROUND);
                break;
            case LEFT:
                RotationHelper.rotate(getRotation(), 0, 2.1f, rotateToVector);
                ParticleSpawner.spawnShipEngineSmoke(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y);
                break;
            case RIGHT:
                RotationHelper.rotate(getRotation(), 0, -2.1f, rotateToVector);
                ParticleSpawner.spawnShipEngineSmoke(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y);
                break;
            case BACKWARD:
                RotationHelper.rotate(getRotation(), 3.7f, 0, rotateToVector);
                ParticleSpawner.spawnShipEngineSmoke(shipPos.x + rotateToVector.x, shipPos.y + rotateToVector.y);
                break;
        }
    }

    @Override
    public TextureRegister getWreckTexture(int textureOffset) {
        return TextureRegister.values()[TextureRegister.particleWreckEngiSmall0Wreck0.ordinal() + textureOffset];
    }

    @Override
    public TextureRegister getWreckFireTexture(int textureOffset) {
        return TextureRegister.values()[TextureRegister.particleWreckEngiSmall0Fire0.ordinal() + textureOffset];
    }

    @Override
    public TextureRegister getWreckLightTexture(int textureOffset) {
        return TextureRegister.values()[TextureRegister.particleWreckEngiSmall0Light0.ordinal() + textureOffset];
    }

    @Override
    protected void createDestroyParticles() {
        ParticleSpawner.spawnDestroyShipSmall(this);
    }
}
