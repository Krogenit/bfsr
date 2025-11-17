package net.bfsr.client.world.entity.spawn;

import lombok.RequiredArgsConstructor;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.engine.world.World;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.network.packet.common.entity.spawn.WreckSpawnData;

@RequiredArgsConstructor
class WreckSpawnLogic implements EntitySpawnLogic<WreckSpawnData> {
    private final WreckRegistry wreckRegistry;
    private final ObjectPool<Wreck> wreckPool;

    @Override
    public void spawn(WreckSpawnData spawnData, World world, ConfigConverterManager configConverterManager, AbstractRenderer renderer) {
        world.add(wreckPool.get().init(world, spawnData.getEntityId(), spawnData.getPosX(), spawnData.getPosY(),
                spawnData.getSin(), spawnData.getCos(), spawnData.getSizeX(), spawnData.getSizeY(), 0.0f, 0.0f, 0.0f,
                spawnData.getMaxLifeTime(), spawnData.getDestroyedShipId(), spawnData.isEmitFire(),
                wreckRegistry.get(spawnData.getDataId())));
    }
}