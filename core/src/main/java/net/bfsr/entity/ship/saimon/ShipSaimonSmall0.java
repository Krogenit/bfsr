package net.bfsr.entity.ship.saimon;

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

public class ShipSaimonSmall0 extends Ship {
    public ShipSaimonSmall0(WorldServer w, Vector2f pos, float rot, boolean spawned) {
        super(w, pos, rot, new Vector2f(100, 100), new Vector3f(1.0f, 0.6f, 0.5f), spawned);
    }

    public ShipSaimonSmall0(WorldClient w, int id, Vector2f pos, float rot) {
        super(w, id, TextureRegister.shipSaimonSmall0, pos, rot, new Vector2f(100, 100), new Vector3f(1.0f, 0.6f, 0.5f));
        this.textureDamage = TextureLoader.getTexture(TextureRegister.shipSaimonSmall0Damage);
        addDamage(new Damage(this, 0.8f, 0, new Vector2f(10, -4), 0.8f));
        addDamage(new Damage(this, 0.6f, 0, new Vector2f(5, -18), 1f));
        addDamage(new Damage(this, 0.4f, 1, new Vector2f(5, 15), 0.55f));
        addDamage(new Damage(this, 0.2f, 3, new Vector2f(-19, 0), 0.6f));
    }

    @Override
    protected void init() {
        this.setEngine(new Engine(0.06f, 0.05f, 0.05f, 6f, 5f, 5f, 0.99f, 2.6f));

        this.setReactor(new Reactor(30f, 9.75f));

        this.setHull(new Hull(25f, 0.025f, this));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(20f, 0.3f, 1.1f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(20f, 0.3f, 1.1f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(20f, 0.3f, 1.1f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(20f, 0.3f, 1.1f));
        this.setArmor(armor);

        this.setShield(new ShieldSmall0(this, new Vector4f(1.0f, 0.6f, 0.5f, 1.0f), 17.5f, 0.64f, 185));

        this.setWeapoinsCount(2);

        this.setCrew(new Crew(2));

        this.setCargo(new Cargo(2));

        this.createWeaponPosition(new Vector2f(15, 23));
        this.createWeaponPosition(new Vector2f(15, -23));

        this.maxDestroingTimer = 60;
        this.maxSparksTimer = 20;
    }

    @Override
    protected void createBody(Vector2f pos) {
        super.createBody(pos);

        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-50.5f, -17.5f);
        vertices[1] = new Vector2(-34.5f, -17.5f);
        vertices[2] = new Vector2(-5.7f, -9.5f);
        vertices[3] = new Vector2(-15f, -0f);
        BodyFixture fixture = new BodyFixture(Geometry.createPolygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        body.addFixture(fixture);

        vertices = new Vector2[4];
        vertices[0] = new Vector2(-15f, 0f);
        vertices[1] = new Vector2(-5.7f, 8.5f);
        vertices[2] = new Vector2(-31f, 16.5f);
        vertices[3] = new Vector2(-50.5f, 16.5f);
        fixture = new BodyFixture(Geometry.createPolygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        body.addFixture(fixture);

        vertices = new Vector2[6];
        vertices[0] = new Vector2(-33f, -9f);
        vertices[1] = new Vector2(40f, -9f);
        vertices[2] = new Vector2(50.6f, -0.5f);
        vertices[3] = new Vector2(40f, 7.4f);
        vertices[4] = new Vector2(-33f, 7.4f);
        vertices[5] = new Vector2(-39f, -0.5f);
        fixture = new BodyFixture(Geometry.createPolygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        body.addFixture(fixture);

        vertices = new Vector2[4];
        vertices[0] = new Vector2(-4f, -9f);
        vertices[1] = new Vector2(-9f, -31f);
        vertices[2] = new Vector2(14f, -31f);
        vertices[3] = new Vector2(14f, -9f);
        fixture = new BodyFixture(Geometry.createPolygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        body.addFixture(fixture);

        vertices = new Vector2[4];
        vertices[0] = new Vector2(-9f, 30.5f);
        vertices[1] = new Vector2(-4f, 7.5f);
        vertices[2] = new Vector2(14f, 7.5f);
        vertices[3] = new Vector2(14f, 30.5f);
        fixture = new BodyFixture(Geometry.createPolygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        body.addFixture(fixture);


        recalculateMass();
        body.translate(pos.x, pos.y);
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    @Override
    public void spawnEngineParticles(Direction dir, double delta) {
        Vector2f shipPos = getPosition();

        switch (dir) {
            case FORWARD:
                Vector2f offset = RotationHelper.rotate(getRotation(), -33, 0);
                Vector2f pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                Vector2 shipVelocity = body.getLinearVelocity();
                Vector2f velocity = new Vector2f((float) shipVelocity.x / 50f, (float) shipVelocity.y / 50f);
                ParticleSpawner.spawnEngineBack(pos, velocity, getRotation(), 100f, 6F, new Vector4f(1f, 0.5f, 0.5f, 1f), true);
                RotationHelper.rotate(getRotation(), -35, 0, offset);
                pos.x = shipPos.x + offset.x;
                pos.y = shipPos.y + offset.y;
                ParticleSpawner.spawnLight(pos, 60f, new Vector4f(1f, 0.5f, 0.5f, 1f), EnumParticlePositionType.Background);
                break;
            case LEFT:
                offset = RotationHelper.rotate(getRotation(), -0, 30);
                pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                ParticleSpawner.spawnShipEngineSmoke(pos);
                break;
            case RIGHT:
                offset = RotationHelper.rotate(getRotation(), -0, -30);
                pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                ParticleSpawner.spawnShipEngineSmoke(pos);
                break;
            case BACKWARD:
                offset = RotationHelper.rotate(getRotation(), 50, 0);
                pos = new Vector2f(shipPos.x + offset.x, shipPos.y + offset.y);
                ParticleSpawner.spawnShipEngineSmoke(pos);
                break;
        }
    }

    @Override
    public TextureRegister getWreckTexture(int textureOffset) {
        return TextureRegister.values()[TextureRegister.particleWreckSaimonSmall0Wreck0.ordinal() + textureOffset];
    }

    @Override
    public TextureRegister getWreckFireTexture(int textureOffset) {
        return TextureRegister.values()[TextureRegister.particleWreckSaimonSmall0Fire0.ordinal() + textureOffset];
    }

    @Override
    public TextureRegister getWreckLightTexture(int textureOffset) {
        return TextureRegister.values()[TextureRegister.particleWreckSaimonSmall0Light0.ordinal() + textureOffset];
    }

    @Override
    protected void createDestroyParticles() {
        ParticleSpawner.spawnDestroyShipSmall(this);
    }
}
