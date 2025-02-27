package net.bfsr.config.entity.ship;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class ShipRegistry extends ConfigToDataConverter<ShipConfig, ShipData> {
    public ShipRegistry() {
        super("entity/ship", ShipConfig.class, (fileName, shipConfig) -> fileName, ShipData::new);
    }
}