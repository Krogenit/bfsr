package net.bfsr.client.entity.ship;

import clipper2.core.PathD;
import clipper2.core.PointD;
import net.bfsr.client.collision.filter.ShipFilter;
import net.bfsr.client.component.Damage;
import net.bfsr.client.component.Shield;
import net.bfsr.client.particle.effect.EngineEffects;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.client.renderer.SpriteRenderer;
import net.bfsr.client.renderer.buffer.BufferType;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.client.renderer.texture.TextureLoader;
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
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.CollisionObjectUtils;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

public class ShipHumanSmall0 extends Ship {
    public ShipHumanSmall0(WorldClient world, int id, float x, float y, float rotation) {
        super(world, id, x, y, rotation, 6.9423f, 6.9423f, 0.5f, 0.6f, 1.0f, TextureRegister.shipHumanSmall0, TextureRegister.shipHumanSmall0Damage);
        addDamage(new Damage(this, 0.8f, 0, new Vector2f(-0.5f, 1.5f), 0.08f));
        addDamage(new Damage(this, 0.6f, 0, new Vector2f(-1.8f, -0.8f), 0.08f));
        addDamage(new Damage(this, 0.4f, 1, new Vector2f(-0.5f, -1.5f), 0.055f));
        addDamage(new Damage(this, 0.2f, 2, new Vector2f(0.8f, -0.2f), 0.05f));
    }

    @Override
    public void init() {
        super.init();
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
    protected void initBody() {
        Vector2[] vertices = new Vector2[7];
        vertices[0] = new Vector2(-2.9f, 0.2f);
        vertices[1] = new Vector2(-2.9f, -0.2f);
        vertices[2] = new Vector2(-1.0f, -3.1f);
        vertices[3] = new Vector2(0.6f, -3.1f);
        vertices[4] = new Vector2(2.7f, 0.1f);
        vertices[5] = new Vector2(0.6f, 3.1f);
        vertices[6] = new Vector2(-1.0f, 3.1f);

        PathD pathD = new PathD(vertices.length);
        for (int i = 0; i < vertices.length; i++) {
            Vector2 vector2 = vertices[i];
            pathD.add(new PointD(vector2.x, vector2.y));
        }
        contours.add(pathD);

        maskTexture = new DamageMaskTexture(texture.getWidth(), texture.getHeight(), BufferUtils.createByteBuffer(texture.getWidth() * texture.getHeight()));
        maskTexture.createWhiteMask();

        BodyFixture fixture = new BodyFixture(new Polygon(vertices));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        fixture.setFilter(new ShipFilter(this));
        body.addFixture(fixture);
        body.setMass(MassType.NORMAL);
        body.setUserData(this);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
    }

    @Override
    public void spawnEngineParticles(Direction direction) {
        Vector2f shipPos = getPosition();

        float rotation = getRotation();
        if (direction == Direction.FORWARD) {
            RotationHelper.rotate(rotation, -2.3f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            Vector2 shipVelocity = body.getLinearVelocity();
            EngineEffects.smallEngine(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, rotation, 10.0f,
                    (float) shipVelocity.x / 50.0f, (float) shipVelocity.y / 50.0f, 0.5f, 0.5f, 1.0f, 1.0f, engineSpawnAccumulator);
        } else if (direction == Direction.LEFT) {
            RotationHelper.rotate(rotation, -0.5f, 3.0f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            EngineEffects.secondaryEngine(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, engineSpawnAccumulator);
        } else if (direction == Direction.RIGHT) {
            RotationHelper.rotate(rotation, -0.5f, -3.0f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            EngineEffects.secondaryEngine(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, engineSpawnAccumulator);
        } else if (direction == Direction.BACKWARD) {
            RotationHelper.rotate(rotation, 3.0f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            EngineEffects.secondaryEngine(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, engineSpawnAccumulator);
        }
    }

    @Override
    protected void createDestroyParticles() {
        ExplosionEffects.spawnDestroyShipSmall(this);
    }

    @Override
    public ShipType getType() {
        return ShipType.HUMAN_SMALL_0;
    }

    @Override
    public void renderAdditive() {
        if (moveDirection == Direction.FORWARD) {
            Vector2f shipPos = getPosition();
            float rotation = getRotation();
            RotationHelper.rotate(rotation, -1.7f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);

            SpriteRenderer.get().add(lastPosition.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, lastPosition.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y,
                    shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, rotation, 6.0f, 6.0f,
                    effectsColor.x, effectsColor.y, effectsColor.z, 0.5f, TextureLoader.getTexture(TextureRegister.particleLight), BufferType.ENTITIES_ADDITIVE);
        }

        super.renderAdditive();
    }
}