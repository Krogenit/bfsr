package net.bfsr.client.world.entity;

import net.bfsr.config.ConfigConverterManager;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.world.World;

@FunctionalInterface
public interface EntitySpawnLogic<T extends EntityPacketSpawnData> {
    void spawn(T spawnData, World world, ConfigConverterManager configConverterManager, AbstractRenderer renderer);
}