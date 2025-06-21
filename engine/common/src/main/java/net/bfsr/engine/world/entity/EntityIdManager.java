package net.bfsr.engine.world.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EntityIdManager {
    private final int startId;
    protected int id;

    public EntityIdManager(int startId) {
        this.startId = startId;
        this.id = startId;
    }

    public EntityIdManager() {
        this(0);
    }

    public void add(RigidBody rigidBody) {}

    public void remove(int index, RigidBody rigidBody) {}

    public void update(double timestamp) {}

    public int getNextId() {
        return id++;
    }

    public int getCurrentId() {
        return id;
    }

    public void clear() {
        id = startId;
    }
}