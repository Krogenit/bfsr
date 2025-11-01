package net.bfsr.client.renderer.entity;

import net.bfsr.engine.Engine;
import net.bfsr.entity.Station;

public class StationRender extends RigidBodyRender {
    private final Station station;

    public StationRender(Station station) {
        super(Engine.getAssetsManager().getTexture(station.getConfigData().getTexture()), station);
        this.station = station;
    }
}
