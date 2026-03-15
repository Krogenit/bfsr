package net.bfsr.config.entity.station;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class StationRegistry extends ConfigToDataConverter<StationConfig, StationData> {
    public StationRegistry() {
        super("entity/station", StationConfig.class, (fileName, stationConfig) -> fileName, StationData::new);
    }
}