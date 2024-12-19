package net.bfsr.world;

import lombok.Getter;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.Side;
import net.bfsr.entity.CommonEntityManager;
import net.bfsr.entity.EntityIdManager;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.world.WorldInitEvent;
import net.bfsr.physics.CollisionMatrix;
import net.bfsr.physics.CommonCollisionHandler;
import net.bfsr.physics.ContactListener;
import net.bfsr.physics.filter.ContactFilter;
import org.jbox2d.common.Vector2;

import java.util.List;
import java.util.Random;

public class World {
    private static final int VELOCITY_ITERATIONS = 1;
    private static final int POSITION_ITERATIONS = 1;

    @Getter
    private org.jbox2d.dynamics.World physicWorld;
    @Getter
    protected final Side side;
    @Getter
    private final long seed;
    private final Profiler profiler;
    @Getter
    protected final Random rand = new Random();
    @Getter
    private final CommonEntityManager entityManager;
    @Getter
    private final EventBus eventBus;
    @Getter
    private double timestamp;
    private final EntityIdManager entityIdManager;
    @Getter
    private final GameLogic gameLogic;
    @Getter
    private final CollisionMatrix collisionMatrix;
    @Getter
    private final ContactFilter contactFilter;
    @Getter
    private final ObjectPools objectPools = new ObjectPools();

    public World(Profiler profiler, Side side, long seed, EventBus eventBus, CommonEntityManager entityManager,
                 EntityIdManager entityIdManager, GameLogic gameLogic, CommonCollisionHandler collisionHandler) {
        this.profiler = profiler;
        this.side = side;
        this.seed = seed;
        this.eventBus = eventBus;
        this.entityManager = entityManager;
        this.entityIdManager = entityIdManager;
        this.gameLogic = gameLogic;
        this.collisionMatrix = new CollisionMatrix(collisionHandler);
        this.contactFilter = new ContactFilter(collisionMatrix);
    }

    private void initPhysicWorld() {
        physicWorld = new org.jbox2d.dynamics.World(new Vector2());
        physicWorld.setContactListener(new ContactListener(collisionMatrix));
        physicWorld.setContactFilter(contactFilter);
    }

    public void init() {
        initPhysicWorld();
        eventBus.register(entityManager.getDataHistoryManager());
        eventBus.publish(new WorldInitEvent(this));
    }

    public void update(double timestamp) {
        this.timestamp = timestamp;

        profiler.start("entityManager");
        entityManager.update();
        profiler.endStart("physics");
        physicWorld.step(gameLogic.getUpdateDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        profiler.endStart("postPhysicsUpdate");
        entityManager.postPhysicsUpdate();
        profiler.end();
    }

    public void add(RigidBody entity) {
        add(entity, true);
    }

    public void add(RigidBody entity, boolean addToPhysicWorld) {
        entityManager.add(entity);

        if (addToPhysicWorld) {
            physicWorld.addBody(entity.getBody());
        }

        entity.onAddedToWorld();
    }

    public void remove(int index, RigidBody entity) {
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

    public List<? extends RigidBody> getEntities() {
        return entityManager.getEntities();
    }

    public <T extends RigidBody> T getEntityById(int id) {
        return (T) entityManager.get(id);
    }

    public <T extends RigidBody> List<T> getEntitiesByType(Class<T> classType) {
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

    public float getUpdateDeltaTime() {
        return gameLogic.getUpdateDeltaTime();
    }
}