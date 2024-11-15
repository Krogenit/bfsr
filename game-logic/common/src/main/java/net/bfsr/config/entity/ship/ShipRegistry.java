package net.bfsr.config.entity.ship;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class ShipRegistry extends ConfigToDataConverter<ShipConfig, ShipData> {
    public ShipRegistry() {
        super("entity/ship", ShipConfig.class, (fileName, shipConfig) -> fileName, ShipData::new);
    }
}