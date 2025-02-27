package net.bfsr.client.world.entity;

import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.engine.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.world.World;

@FunctionalInterface
public interface EntitySpawnLogic<T extends EntityPacketSpawnData> {
    void spawn(T spawnData, World world, ConfigConverterManager configConverterManager, AbstractRenderer renderer);
}