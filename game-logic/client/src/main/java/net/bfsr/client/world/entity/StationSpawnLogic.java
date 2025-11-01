package net.bfsr.client.world.entity;

import lombok.RequiredArgsConstructor;
import net.bfsr.config.entity.station.StationData;
import net.bfsr.config.entity.station.StationRegistry;
import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.world.World;
import net.bfsr.entity.Station;
import net.bfsr.network.packet.common.entity.spawn.StationSpawnData;

@RequiredArgsConstructor
public class StationSpawnLogic implements EntitySpawnLogic<StationSpawnData> {
    private final StationRegistry stationRegistry;

    @Override
    public void spawn(StationSpawnData spawnData, World world, ConfigConverterManager configConverterManager, AbstractRenderer renderer) {
        StationData stationData = stationRegistry.get(spawnData.getDataId());
        Station station = new Station(stationData);
        station.init(world, spawnData.getEntityId());
        world.add(station);
    }
}