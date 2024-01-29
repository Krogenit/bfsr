package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.network.packet.common.entity.spawn.WreckSpawnData;
import net.bfsr.world.World;

public class WreckSpawnLogic implements EntitySpawnLogic<WreckSpawnData> {
    @Override
    public void spawn(WreckSpawnData spawnData) {
        World world = Core.get().getWorld();
        world.add(Wreck.WREAK_POOL.get().init(world, spawnData.getEntityId(), spawnData.getWreckIndex(), spawnData.isLight(),
                spawnData.isFire(), spawnData.isFireExplosion(), spawnData.getPosX(), spawnData.getPosY(),
                spawnData.getVelocity().x, spawnData.getVelocity().y, spawnData.getSin(), spawnData.getCos(),
                spawnData.getRotationSpeed(), spawnData.getSize().x, spawnData.getSize().y, spawnData.getMaxLifeTime(),
                spawnData.getWreckType())
        );
    }
}