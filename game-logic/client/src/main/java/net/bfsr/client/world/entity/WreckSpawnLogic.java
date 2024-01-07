package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.WreckSpawnData;
import net.bfsr.world.World;

public class WreckSpawnLogic implements EntitySpawnLogic {
    @Override
    public void spawn(EntityPacketSpawnData spawnData) {
        World world = Core.get().getWorld();
        WreckSpawnData wreckSpawnData = (WreckSpawnData) spawnData;
        world.add(Wreck.WREAK_POOL.get().init(world, spawnData.getEntityId(), wreckSpawnData.getWreckIndex(),
                wreckSpawnData.isLight(), wreckSpawnData.isFire(), wreckSpawnData.isFireExplosion(), spawnData.getPosX(),
                spawnData.getPosY(), wreckSpawnData.getVelocity().x, wreckSpawnData.getVelocity().y, spawnData.getSin(),
                spawnData.getCos(), wreckSpawnData.getRotationSpeed(), wreckSpawnData.getSize().x, wreckSpawnData.getSize().y,
                wreckSpawnData.getAlphaVelocity(), wreckSpawnData.getWreckType())
        );
    }
}