package net.bfsr.world;

import lombok.Getter;
import net.bfsr.engine.GameLogic;
import net.bfsr.engine.event.EventBusManager;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.Side;
import net.bfsr.entity.EntityIdManager;
import net.bfsr.entity.EntityManager;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.physics.CCDTransformHandler;
import net.bfsr.physics.ContactListener;
import net.bfsr.physics.CustomValueMixer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.world.PhysicsWorld;

import java.util.List;
import java.util.Random;

public class World {
    @Getter
    private org.dyn4j.world.World<Body> physicWorld;
    private final CCDTransformHandler ccdTransformHandler = new CCDTransformHandler();
    @Getter
    protected final Side side;
    @Getter
    private final long seed;
    private final Profiler profiler;
    @Getter
    protected final Random rand = new Random();
    @Getter
    private final EntityManager entityManager = new EntityManager();
    @Getter
    private final EventBusManager eventBus;
    @Getter
    private double timestamp;
    private final EntityIdManager entityIdManager;
    @Getter
    private final GameLogic gameLogic;

    public World(Profiler profiler, Side side, long seed, EventBusManager eventBus, EntityIdManager entityIdManager,
                 GameLogic gameLogic) {
        this.profiler = profiler;
        this.side = side;
        this.seed = seed;
        this.eventBus = eventBus;
        this.entityIdManager = entityIdManager;
        this.gameLogic = gameLogic;
    }

    private void initPhysicWorld() {
        physicWorld = new org.dyn4j.world.World<>();
        physicWorld.setGravity(PhysicsWorld.ZERO_GRAVITY);
        physicWorld.getSettings().setMaximumTranslation(30);
        physicWorld.getSettings().setPositionConstraintSolverIterations(1);
        physicWorld.getSettings().setVelocityConstraintSolverIterations(1);
        physicWorld.getSettings().setStepFrequency(gameLogic.getUpdateDeltaTime());
        physicWorld.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY);
        physicWorld.addContactListener(new ContactListener());
        physicWorld.addTimeOfImpactListener(ccdTransformHandler);
        physicWorld.setValueMixer(new CustomValueMixer());
    }

    public void init() {
        initPhysicWorld();
        this.eventBus.register(entityManager.getDataHistoryManager());
    }

    public void update(double timestamp) {
        this.timestamp = timestamp;

        entityManager.update();
        profiler.endStartSection("physics");
        ccdTransformHandler.clear();
        physicWorld.step(1);
        ccdTransformHandler.restoreTransforms();
        profiler.endStartSection("postPhysicsUpdate");
        entityManager.postPhysicsUpdate();
    }

    public void add(RigidBody<?> entity) {
        add(entity, true);
    }

    public void add(RigidBody<?> entity, boolean addToPhysicWorld) {
        entityManager.add(entity);

        if (addToPhysicWorld) {
            physicWorld.addBody(entity.getBody());
        }

        entity.onAddedToWorld();
    }

    public void remove(int index, RigidBody<?> entity) {
        entityManager.remove(index, entity);
        physicWorld.removeBody(entity.getBody());
        entity.onRemovedFromWorld();
    }

    public int convertToTicks(int value) {
        return gameLogic.convertToTicks(value);
    }

    public int convertToTicks(float value) {
        return gameLogic.convertToTicks(value);
    }

    public float convertToDeltaTime(float value) {
        return gameLogic.convertToTicks(value);
    }

    public void clear() {
        eventBus.unregister(entityManager.getDataHistoryManager());
        entityManager.clear();
        physicWorld.removeAllBodies();
    }

    public int getBulletsCount() {
        return entityManager.get(Bullet.class).size();
    }

    public int getWreckCount() {
        return entityManager.get(Wreck.class).size();
    }

    public int getShipWreckCount() {
        return entityManager.get(ShipWreck.class).size();
    }

    public List<? extends RigidBody<?>> getEntities() {
        return entityManager.getEntities();
    }

    public RigidBody<?> getEntityById(int id) {
        return entityManager.get(id);
    }

    public <T extends RigidBody<?>> List<T> getEntitiesByType(Class<T> classType) {
        return (List<T>) entityManager.get(classType);
    }

    public int getNextId() {
        return entityIdManager.getNextId();
    }

    public boolean isServer() {
        return side.isServer();
    }

    public boolean isClient() {
        return side.isClient();
    }
}