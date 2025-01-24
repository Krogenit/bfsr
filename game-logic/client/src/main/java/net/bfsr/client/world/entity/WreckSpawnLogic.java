package net.bfsr.client.world.entity;

import lombok.RequiredArgsConstructor;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.network.packet.common.entity.spawn.WreckSpawnData;
import net.bfsr.world.World;

@RequiredArgsConstructor
public class WreckSpawnLogic implements EntitySpawnLogic<WreckSpawnData> {
    private final WreckRegistry wreckRegistry;

    @Override
    public void spawn(WreckSpawnData spawnData, World world, ConfigConverterManager configConverterManager, AbstractRenderer renderer) {
        world.add(world.getObjectPools().getWrecksPool().get().init(world, spawnData.getEntityId(), spawnData.getWreckIndex(),
                spawnData.isLight(), spawnData.isFire(), spawnData.isFireExplosion(), spawnData.getPosX(), spawnData.getPosY(),
                spawnData.getVelocityX(), spawnData.getVelocityY(), spawnData.getSin(), spawnData.getCos(),
                spawnData.getRotationSpeed(), spawnData.getSizeX(), spawnData.getSizeY(), spawnData.getMaxLifeTime(),
                spawnData.getWreckType(), wreckRegistry.getWreck(spawnData.getWreckType(), spawnData.getWreckIndex()))
        );
    }
}