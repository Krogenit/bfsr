package net.bfsr.entity;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;

import java.util.ArrayList;
import java.util.List;

public class CommonEntityManager {
    private final TIntObjectMap<RigidBody<?>> entitiesById = new TIntObjectHashMap<>();
    @SuppressWarnings("rawtypes")
    private final TMap<Class<? extends RigidBody>, List<RigidBody<?>>> entitiesByClass = new THashMap<>();
    @Getter
    protected final List<RigidBody<?>> entities = new ArrayList<>();
    @Getter
    private final EntityDataHistoryManager dataHistoryManager = new EntityDataHistoryManager();

    public CommonEntityManager() {
        this.entitiesByClass.put(RigidBody.class, new ArrayList<>());
        this.entitiesByClass.put(Ship.class, new ArrayList<>());
        this.entitiesByClass.put(Bullet.class, new ArrayList<>());
        this.entitiesByClass.put(ShipWreck.class, new ArrayList<>());
        this.entitiesByClass.put(Wreck.class, new ArrayList<>());
    }

    public void update() {
        for (int i = 0; i < entities.size(); i++) {
            RigidBody<?> rigidBody = entities.get(i);
            if (rigidBody.isDead()) {
                rigidBody.world.remove(i--, rigidBody);
            } else {
                rigidBody.update();
            }
        }
    }

    public void postPhysicsUpdate() {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).postPhysicsUpdate();
        }
    }

    public void add(RigidBody<?> entity) {
        if (entitiesById.containsKey(entity.getId())) {
            throw new RuntimeException("Entity with id " + entity.getId() + " already registered!");
        }

        entitiesById.put(entity.getId(), entity);
        entities.add(entity);
        entitiesByClass.get(entity.getClass()).add(entity);
    }

    public void remove(int index, RigidBody<?> entity) {
        entities.remove(index);
        entitiesById.remove(entity.getId());
        entitiesByClass.get(entity.getClass()).remove(entity);
    }

    public void clear() {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).onRemovedFromWorld();
        }

        entitiesByClass.forEachValue(rigidBodies -> {
            rigidBodies.clear();
            return true;
        });

        entities.clear();
        entitiesById.clear();
        dataHistoryManager.clear();
    }

    public List<? extends RigidBody<?>> get(Class<?> entityClass) {
        return entitiesByClass.get(entityClass);
    }

    public RigidBody<?> get(int id) {
        return entitiesById.get(id);
    }
}