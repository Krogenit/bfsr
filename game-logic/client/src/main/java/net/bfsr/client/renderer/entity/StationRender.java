package net.bfsr.client.renderer.entity;

import net.bfsr.engine.Engine;
import net.bfsr.entity.Station;

public class StationRender extends RigidBodyRender {
    private final Station station;

    public StationRender(Station station, float z) {
        super(station, z, Engine.getAssetsManager().getTexture(station.getConfigData().getTextureData()));
        this.station = station;
    }
}
