package net.bfsr.world;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import net.bfsr.engine.GameLogic;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.Side;
import net.bfsr.entity.EntityIdManager;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.physics.CCDTransformHandler;
import net.bfsr.physics.ContactListener;
import net.bfsr.physics.CustomValueMixer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.world.PhysicsWorld;

import java.util.ArrayList;
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

    private final TIntObjectMap<RigidBody<?>> entitiesById = new TIntObjectHashMap<>();
    private final TMap<Class<? extends RigidBody>, List<RigidBody<?>>> entitiesByClass = new THashMap<>();
    @Getter
    private final List<RigidBody<?>> entities = new ArrayList<>();
    @Getter
    private final EventBus eventBus;
    @Getter
    private double timestamp;
    private final EntityIdManager entityIdManager;
    @Getter
    private final GameLogic gameLogic;

    public World(Profiler profiler, Side side, long seed, EventBus eventBus, EntityIdManager entityIdManager,
                 GameLogic gameLogic) {
        this.profiler = profiler;
        this.side = side;
        this.seed = seed;
        this.eventBus = eventBus;
        this.entityIdManager = entityIdManager;
        this.gameLogic = gameLogic;
        this.entitiesByClass.put(RigidBody.class, new ArrayList<>());
        this.entitiesByClass.put(Ship.class, new ArrayList<>());
        this.entitiesByClass.put(Bullet.class, new ArrayList<>());
        this.entitiesByClass.put(ShipWreck.class, new ArrayList<>());
        this.entitiesByClass.put(Wreck.class, new ArrayList<>());
        initPhysicWorld();
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

    public void update(double timestamp) {
        this.timestamp = timestamp;

        updateEntities();
        profiler.endStartSection("physics");
        ccdTransformHandler.clear();
        physicWorld.step(1);
        ccdTransformHandler.restoreTransforms();
        profiler.endStartSection("postPhysicsUpdate");
        postPhysicsUpdate();
    }

    private void updateEntities() {
        for (int i = 0; i < entities.size(); i++) {
            RigidBody<?> rigidBody = entities.get(i);
            if (rigidBody.isDead()) {
                remove(i--, rigidBody);
            } else {
                rigidBody.update();
            }
        }
    }

    private void postPhysicsUpdate() {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).postPhysicsUpdate();
        }
    }

    public void add(RigidBody<?> entity) {
        add(entity, true);
    }

    public void add(RigidBody<?> entity, boolean addToPhysicWorld) {
        if (entitiesById.containsKey(entity.getId())) {
            throw new RuntimeException("Entity with id " + entity.getId() + " already registered!");
        }

        entitiesById.put(entity.getId(), entity);
        entities.add(entity);
        entitiesByClass.get(entity.getClass()).add(entity);

        if (addToPhysicWorld) {
            physicWorld.addBody(entity.getBody());
        }

        entity.onAddedToWorld();
    }

    public void remove(int index, RigidBody<?> entity) {
        entities.remove(index);
        entitiesById.remove(entity.getId());
        physicWorld.removeBody(entity.getBody());
        entity.onRemovedFromWorld();
        entitiesByClass.get(entity.getClass()).remove(entity);
    }

    public void clear() {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).onRemovedFromWorld();
        }

        entities.clear();
        entitiesById.clear();
        physicWorld.removeAllBodies();

        entitiesByClass.forEachValue(rigidBodies -> {
            rigidBodies.clear();
            return true;
        });
    }

    public int getBulletsCount() {
        return entitiesByClass.get(Bullet.class).size();
    }

    public int getWreckCount() {
        return entitiesByClass.get(Wreck.class).size();
    }

    public int getShipWreckCount() {
        return entitiesByClass.get(ShipWreck.class).size();
    }

    public RigidBody<?> getEntityById(int id) {
        return entitiesById.get(id);
    }

    public <T extends RigidBody<?>> List<T> getEntitiesByType(Class<T> classType) {
        return (List<T>) entitiesByClass.get(classType);
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

    public int convertToTicks(int value) {
        return gameLogic.convertToTicks(value);
    }

    public int convertToTicks(float value) {
        return gameLogic.convertToTicks(value);
    }

    public float convertToDeltaTime(float value) {
        return gameLogic.convertToTicks(value);
    }
}