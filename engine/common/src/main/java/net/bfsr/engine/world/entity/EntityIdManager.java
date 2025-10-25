package net.bfsr.engine.world.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class EntityIdManager {
    private final int startId;
    @Getter
    protected int id;

    public EntityIdManager(int startId) {
        this.startId = startId;
        this.id = startId;
    }

    public EntityIdManager() {
        this(0);
    }

    public void update(int frame) {}

    public int getNextId() {
        return id++;
    }

    public void clear() {
        id = startId;
    }
}