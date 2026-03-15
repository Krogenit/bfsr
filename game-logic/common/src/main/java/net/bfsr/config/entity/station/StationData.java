package net.bfsr.config.entity.station;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.entity.GameObjectConfigData;

@Getter
public class StationData extends GameObjectConfigData {
    private final int destroyTimeInFrames;
    private final float shieldOutlineOffset;
    private final float shieldBlurSize;

    public StationData(StationConfig stationConfig, String fileName, int id, int registryId) {
        super(stationConfig, fileName, id, registryId);
        this.destroyTimeInFrames = Engine.convertSecondsToFrames(stationConfig.getDestroyTimeInSeconds());
        this.shieldOutlineOffset = stationConfig.getShieldOutlineOffset();
        this.shieldBlurSize = stationConfig.getShieldBlurSize();
    }
}