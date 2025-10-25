package net.bfsr.config.component.hull;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class HullRegistry extends ConfigToDataConverter<HullConfig, HullData> {
    public HullRegistry() {
        super("module/hull", HullConfig.class, (fileName, hullConfig) -> fileName, HullData::new);
    }
}