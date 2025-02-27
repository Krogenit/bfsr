package net.bfsr.config.component.shield;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class ShieldRegistry extends ConfigToDataConverter<ShieldConfig, ShieldData> {
    public ShieldRegistry() {
        super("module/shield", ShieldConfig.class, (fileName, shieldConfig) -> fileName, ShieldData::new);
    }
}