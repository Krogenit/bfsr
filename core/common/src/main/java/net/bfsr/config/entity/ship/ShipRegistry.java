package net.bfsr.config.entity.ship;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class ShipRegistry extends ConfigToDataConverter<ShipConfig, ShipData> {
    public static final ShipRegistry INSTANCE = new ShipRegistry();

    public ShipRegistry() {
        super("entity/ship", ShipConfig.class, ShipConfig::name, ShipData::new);
    }
}