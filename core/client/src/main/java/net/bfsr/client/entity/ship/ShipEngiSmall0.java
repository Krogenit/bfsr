package net.bfsr.client.entity.ship;

import clipper2.core.PathD;
import clipper2.core.PointD;
import net.bfsr.client.collision.filter.ShipFilter;
import net.bfsr.client.component.Shield;
import net.bfsr.client.particle.SpawnAccumulator;
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

public class ShipEngiSmall0 extends Ship {
    private final SpawnAccumulator leftBackEngineSpawnAccumulator = new SpawnAccumulator();
    private final SpawnAccumulator rightBackEngineSpawnAccumulator = new SpawnAccumulator();

    public ShipEngiSmall0(WorldClient world, int id, float x, float y, float rotation) {
        super(world, id, x, y, rotation, 13.913f, 13.913f, 0.8f, 1.0f, 0.5f, TextureRegister.shipEngiSmall0, TextureRegister.shipEngiSmall0Damage);
    }

    @Override
    public void init() {
        super.init();
        setEngine(new Engine(1.2f, 1.0f, 1.0f, 30.0f, 0.99f, 2.5f));
        setReactor(new Reactor(30.0f, 9.0f));
        setHull(new Hull(22.5f, 0.0225f));

        Armor armor = new Armor();
        armor.setArmorPlateByDir(Direction.FORWARD, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.BACKWARD, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.LEFT, new ArmorPlate(17.5f, 0.25f, 1.05f));
        armor.setArmorPlateByDir(Direction.RIGHT, new ArmorPlate(17.5f, 0.25f, 1.05f));
        setArmor(armor);
        ShieldConfig shieldConfig = ShieldRegistry.INSTANCE.getShield("engiSmall0");
        Shield shield = new Shield(this, shieldConfig, 0.8f, 1.0f, 0.5f, 1.0f);
        setShield(shield);
        shield.createBody();
        setWeaponsCount(2);
        setCrew(new Crew(2));
        setCargo(new Cargo(2));
        createWeaponPosition(new Vector2f(1.8f, 1.8f));
        createWeaponPosition(new Vector2f(1.8f, -1.8f));

        maxDestroyingTimer = 60;
        maxSparksTimer = 20;
    }

    @Override
    protected void initBody() {
        Vector2[] vertices = new Vector2[7];
        vertices[0] = new Vector2(-3.6f, 0.0f);
        vertices[1] = new Vector2(-1.7f, -2.0f);
        vertices[2] = new Vector2(1.0f, -2.0f);
        vertices[3] = new Vector2(3.6f, -0.55f);
        vertices[4] = new Vector2(3.6f, 0.55f);
        vertices[5] = new Vector2(1.0f, 2.0f);
        vertices[6] = new Vector2(-1.7f, 2.0f);

        PathD pathD = new PathD(vertices.length);
        for (int i = 0; i < vertices.length; i++) {
            Vector2 vector2 = vertices[i];
            pathD.add(new PointD(vector2.x, vector2.y));
        }
        contours.add(pathD);

        maskTexture = new DamageMaskTexture(texture.getWidth(), texture.getHeight(), BufferUtils.createByteBuffer(texture.getWidth() * texture.getHeight()));
        maskTexture.createWhiteMask();

        BodyFixture fixture = new BodyFixture(new Polygon(vertices));
        fixture.setFilter(new ShipFilter(this));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
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
            RotationHelper.rotate(rotation, -3.7f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            Vector2 shipVelocity = body.getLinearVelocity();
            float velocityX = (float) shipVelocity.x / 50.0f;
            float velocityY = (float) shipVelocity.y / 50.0f;
            EngineEffects.smallEngine(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, rotation, 10.0f, velocityX, velocityY,
                    effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, engineSpawnAccumulator);

            RotationHelper.rotate(rotation, -3.0f, 1.1f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            EngineEffects.smallEngineNoSmoke(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, rotation, 10.0f, velocityX, velocityY,
                    effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, leftBackEngineSpawnAccumulator);
            RotationHelper.rotate(rotation, -3.0f, -1.1f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            EngineEffects.smallEngineNoSmoke(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, rotation, 10.0f, velocityX, velocityY,
                    effectsColor.x, effectsColor.y, effectsColor.z, 1.0f, rightBackEngineSpawnAccumulator);
        } else if (direction == Direction.LEFT) {
            RotationHelper.rotate(rotation, 0, 2.1f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            EngineEffects.secondaryEngine(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, engineSpawnAccumulator);
        } else if (direction == Direction.RIGHT) {
            RotationHelper.rotate(rotation, 0, -2.1f, CollisionObjectUtils.ROTATE_TO_VECTOR);
            EngineEffects.secondaryEngine(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, engineSpawnAccumulator);
        } else if (direction == Direction.BACKWARD) {
            RotationHelper.rotate(rotation, 3.7f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);
            EngineEffects.secondaryEngine(shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, engineSpawnAccumulator);
        }
    }

    @Override
    protected void createDestroyParticles() {
        ExplosionEffects.spawnDestroyShipSmall(this);
    }

    @Override
    public ShipType getType() {
        return ShipType.ENGI_SMALL_0;
    }

    @Override
    public void renderAdditive() {
        if (moveDirection == Direction.FORWARD) {
            Vector2f shipPos = getPosition();
            float rotation = getRotation();
            RotationHelper.rotate(rotation, -2.7f, 0, CollisionObjectUtils.ROTATE_TO_VECTOR);

            SpriteRenderer.get().add(lastPosition.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, lastPosition.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y,
                    shipPos.x + CollisionObjectUtils.ROTATE_TO_VECTOR.x, shipPos.y + CollisionObjectUtils.ROTATE_TO_VECTOR.y, rotation, 6.0f, 6.0f,
                    effectsColor.x, effectsColor.y, effectsColor.z, 0.5f, TextureLoader.getTexture(TextureRegister.particleLight), BufferType.ENTITIES_ADDITIVE);
        }

        super.renderAdditive();
    }

    @Override
    public void setMoveDirection(Direction dir) {
        super.setMoveDirection(dir);
        rightBackEngineSpawnAccumulator.resetTime();
        leftBackEngineSpawnAccumulator.resetTime();
    }
}