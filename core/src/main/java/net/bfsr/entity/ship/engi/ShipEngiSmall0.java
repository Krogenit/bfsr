package net.bfsr.entity.ship.engi;

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
import net.bfsr.world.WorldClient;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShipEngiSmall0 extends Ship {
    public ShipEngiSmall0(WorldServer w, Vector2f pos, float rot, boolean spawned) {
        super(w, pos, rot, new Vector2f(75, 75), new Vector3f(0.8f, 1.0f, 0.5f), spawned);
    }

    public ShipEngiSmall0(WorldClient w, int id, Vector2f pos, float rot) {
        super(w, id, TextureRegister.shipEngiSmall0, pos, rot, new Vector2f(75, 75), new Vector3f(0.8f, 1.0f, 0.5f));
        this.textureDamage = TextureLoader.getTexture(TextureRegister.shipEngiSmall0Damage);
        addDamage(new Damage(this, 0.8f, 0, new Vector2f(-10, 15), 0.8f));
        addDamage(new Damage(this, 0.6f, 0, new Vector2f(5, -12), 0.8f));
        addDamage(new Damage(this, 0.4f, 1, new Vector2f(-15, -0), 0.55f));
        addDamage(new Damage(this, 0.2f, 2, new Vector2f(12, 5), 0.5f));
    }

    @Override
    protected void init() {
        this.setEngine(new Engine(0.06f, 0.05f, 0.05f, 7f, 5f, 5f, 0.99f, 2.5f));
        this.setReactor(new Reactor(30f, 9f));
        this.setHull(new Hull(22.5f, 0.0225f, this));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(17.5f, 0.25f, 1.05f));
        this.setArmor(armor);
        this.setShield(new ShieldSmall0(this, new Vector4f(0.8f, 1.0f, 0.5f, 1.0f), 12.5f, 0.55f, 225f));
        this.setWeaponsCount(2);
        this.setCrew(new Crew(2));
        this.setCargo(new Cargo(2));
        this.createWeaponPosition(new Vector2f(18f, 18f));
        this.createWeaponPosition(new Vector2f(18f, -18f));

        this.maxDestroingTimer = 60;
        this.maxSparksTimer = 20;
    }

    @Override
    protected void createBody(Vector2f pos) {
        super.createBody(pos);

        Vector2[] vertices = new Vector2[7];
        vertices[0] = new Vector2(-36f, 0f);
        vertices[1] = new Vector2(-17f, -20f);
        vertices[2] = new Vector2(10f, -20f);
        vertices[3] = new Vector2(36f, -5.5f);
        vertices[4] = new Vector2(36f, 5.5f);
        vertices[5] = new Vector2(10f, 20f);
        vertices[6] = new Vector2(-17f, 20f);
        BodyFixture fixture = new BodyFixture(Geometry.createPolygon(vertices));
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
                Vector2f offset = RotationHelper.rotate(getRotation(), -37f, 0);
                Vector2f pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                Vector2 shipVelocity = body.getLinearVelocity();
                Vector2f velocity = new Vector2f((float) shipVelocity.x / 50f, (float) shipVelocity.y / 50f);
                ParticleSpawner.spawnEngineBack(pos, velocity, getRotation(), 100f, 6F, new Vector4f(0.8f, 1.0f, 0.5f, 1f), true);
                RotationHelper.rotate(getRotation(), -30f, 11f, offset);
                pos.x = shipPos.x + offset.x;
                pos.y = shipPos.y + offset.y;
                ParticleSpawner.spawnEngineBack(pos, velocity, getRotation(), 100f, 6F, new Vector4f(0.8f, 1.0f, 0.5f, 1f), false);
                RotationHelper.rotate(getRotation(), -30f, -11f, offset);
                pos.x = shipPos.x + offset.x;
                pos.y = shipPos.y + offset.y;
                ParticleSpawner.spawnEngineBack(pos, velocity, getRotation(), 100f, 6F, new Vector4f(0.8f, 1.0f, 0.5f, 1f), false);
                RotationHelper.rotate(getRotation(), -27f, 0, offset);
                pos.x = shipPos.x + offset.x;
                pos.y = shipPos.y + offset.y;
                ParticleSpawner.spawnLight(pos, 60f, new Vector4f(0.8f, 1.0f, 0.5f, 1f), EnumParticlePositionType.Background);
                break;
            case LEFT:
                offset = RotationHelper.rotate(getRotation(), 0, 21f);
                pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                ParticleSpawner.spawnShipEngineSmoke(pos);
                break;
            case RIGHT:
                offset = RotationHelper.rotate(getRotation(), 0, -21f);
                pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                ParticleSpawner.spawnShipEngineSmoke(pos);
                break;
            case BACKWARD:
                offset = RotationHelper.rotate(getRotation(), 37f, 0);
                pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                ParticleSpawner.spawnShipEngineSmoke(pos);
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
