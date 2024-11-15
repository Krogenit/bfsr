package net.bfsr.config.component.shield;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class ShieldRegistry extends ConfigToDataConverter<ShieldConfig, ShieldData> {
    public ShieldRegistry() {
        super("module/shield", ShieldConfig.class, (fileName, shieldConfig) -> fileName, ShieldData::new);
    }
}