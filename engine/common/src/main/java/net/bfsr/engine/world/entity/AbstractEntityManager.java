package net.bfsr.engine.world.entity;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEntityManager {
    @Getter
    private final TIntObjectMap<RigidBody> entitiesById = new TIntObjectHashMap<>();
    @SuppressWarnings("rawtypes")
    private final TMap<Class<? extends RigidBody>, List<RigidBody>> entitiesByClass = new THashMap<>();
    @Getter
    protected final List<RigidBody> entities = new ArrayList<>();
    @Getter
    private final EntityDataHistoryManager dataHistoryManager = new EntityDataHistoryManager();

    protected AbstractEntityManager() {
        this.entitiesByClass.put(RigidBody.class, new ArrayList<>());
        registerEntities();
    }

    public abstract void registerEntities();

    protected <T extends RigidBody> void registerEntity(Class<T> entityClass) {
        this.entitiesByClass.put(entityClass, new ArrayList<>());
    }

    public void update(double timestamp, int frame) {
        for (int i = 0; i < entities.size(); i++) {
            RigidBody rigidBody = entities.get(i);
            if (rigidBody.isDead()) {
                rigidBody.world.remove(i--, rigidBody, frame);
            } else {
                rigidBody.update();
            }
        }
    }

    public void postPhysicsUpdate() {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).processFixturesToRemove();
        }

        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).processFixturesToAdd();
        }

        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).postPhysicsUpdate();
        }
    }

    public void add(RigidBody entity, boolean force) {
        if (!force && entitiesById.containsKey(entity.getId())) {
            throw new RuntimeException("Entity with id " + entity.getId() + " already registered!");
        }

        entitiesById.put(entity.getId(), entity);
        entities.add(entity);
        entitiesByClass.get(entity.getClass()).add(entity);
    }

    public void remove(int index, RigidBody entity) {
        entities.remove(index);
        entitiesById.remove(entity.getId());
        entitiesByClass.get(entity.getClass()).remove(entity);
    }

    public void clear() {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).onRemovedFromWorld(0);
        }

        entitiesByClass.forEachValue(rigidBodies -> {
            rigidBodies.clear();
            return true;
        });

        entities.clear();
        entitiesById.clear();
        dataHistoryManager.clear();
    }

    public List<? extends RigidBody> get(Class<?> entityClass) {
        return entitiesByClass.get(entityClass);
    }

    public RigidBody get(int id) {
        return entitiesById.get(id);
    }
}