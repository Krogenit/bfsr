package net.bfsr.client.world.entity;

import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;

@FunctionalInterface
public interface EntitySpawnLogic<T extends EntityPacketSpawnData> {
    void spawn(T spawnData);
}