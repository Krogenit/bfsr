package net.bfsr.config.component.hull;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class HullRegistry extends ConfigToDataConverter<HullConfig, HullData> {
    public HullRegistry() {
        super("module/hull", HullConfig.class, (fileName, hullConfig) -> fileName, HullData::new);
    }
}