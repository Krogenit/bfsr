package net.bfsr.client.world.entity;

import net.bfsr.engine.world.entity.EntityIdManager;

public class ClientEntityIdManager extends EntityIdManager {
    public ClientEntityIdManager() {
        super(-1);
    }

    @Override
    public int getNextId() {
        return nextId--;
    }
}