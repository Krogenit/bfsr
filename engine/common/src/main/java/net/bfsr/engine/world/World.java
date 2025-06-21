package net.bfsr.engine.world;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.world.WorldInitEvent;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.physics.collision.AbstractCollisionMatrix;
import net.bfsr.engine.physics.collision.ContactListener;
import net.bfsr.engine.physics.collision.filter.ContactFilter;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.world.entity.AbstractEntityManager;
import net.bfsr.engine.world.entity.EntityIdManager;
import net.bfsr.engine.world.entity.RigidBody;
import org.jbox2d.common.Settings;

import java.util.List;

public class World {
    private static final int VELOCITY_ITERATIONS = 1;
    private static final int POSITION_ITERATIONS = 1;

    @Getter
    private final org.jbox2d.dynamics.World physicWorld = new org.jbox2d.dynamics.World();
    @Getter
    private final long seed;
    private final Profiler profiler;
    @Getter
    private final AbstractEntityManager entityManager;
    @Getter
    private final EventBus eventBus;
    @Getter
    private double timestamp;
    @Getter
    private final EntityIdManager entityIdManager;
    @Getter
    private final GameLogic gameLogic;
    @Getter
    private final AbstractCollisionMatrix collisionMatrix;
    @Getter
    private final ContactFilter contactFilter;

    public World(Profiler profiler, long seed, EventBus eventBus, AbstractEntityManager entityManager,
                 EntityIdManager entityIdManager, GameLogic gameLogic, AbstractCollisionMatrix collisionMatrix) {
        this.profiler = profiler;
        this.seed = seed;
        this.eventBus = eventBus;
        this.entityManager = entityManager;
        this.entityIdManager = entityIdManager;
        this.gameLogic = gameLogic;
        this.collisionMatrix = collisionMatrix;
        this.contactFilter = new ContactFilter(collisionMatrix);
        this.physicWorld.setContactListener(new ContactListener(collisionMatrix));
        this.physicWorld.setContactFilter(contactFilter);
        Settings.maxTranslation = Engine.convertToDeltaTime(120);
        Settings.maxTranslationSquared = Settings.maxTranslation * Settings.maxTranslation;
    }

    public void init() {
        eventBus.register(entityManager.getDataHistoryManager());
        eventBus.publish(new WorldInitEvent(this));
    }

    public void update(double timestamp) {
        this.timestamp = timestamp;

        profiler.start("entityManager");
        entityManager.update(timestamp);
        profiler.endStart("physics");
        physicWorld.step(Engine.getUpdateDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        profiler.endStart("postPhysicsUpdate");
        entityManager.postPhysicsUpdate();
        profiler.endStart("entityIdManager");
        entityIdManager.update(timestamp);
        profiler.end();
    }

    public void add(RigidBody entity) {
        add(entity, true);
    }

    public void add(RigidBody entity, boolean addToPhysicWorld) {
        if (addToPhysicWorld) {
            if (physicWorld.isLocked()) {
                gameLogic.addFutureTask(() -> add(entity, true));
                return;
            } else {
                physicWorld.addBody(entity.getBody());
            }
        }

        entityManager.add(entity);
        entityIdManager.add(entity);
        entity.onAddedToWorld();
    }

    public void remove(int index, RigidBody entity) {
        entityManager.remove(index, entity);
        entityIdManager.remove(index, entity);
        physicWorld.removeBody(entity.getBody());
        entity.onRemovedFromWorld();
    }

    public void clear() {
        eventBus.unregister(entityManager.getDataHistoryManager());
        entityManager.clear();
        entityIdManager.clear();

        // Only happens when server is crashed
        if (!physicWorld.isLocked()) {
            physicWorld.removeAllBodies();
        }
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
        return gameLogic.isServer();
    }

    public boolean isClient() {
        return gameLogic.isClient();
    }
}