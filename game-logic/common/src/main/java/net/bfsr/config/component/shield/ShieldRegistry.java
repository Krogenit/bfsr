package net.bfsr.config.component.shield;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class ShieldRegistry extends ConfigToDataConverter<ShieldConfig, ShieldData> {
    public static final ShieldRegistry INSTANCE = new ShieldRegistry();

    public ShieldRegistry() {
        super("module/shield", ShieldConfig.class, ShieldConfig::name, ShieldData::new);
    }
}