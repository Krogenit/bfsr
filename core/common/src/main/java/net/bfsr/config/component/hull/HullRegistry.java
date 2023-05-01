package net.bfsr.config.component.hull;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class HullRegistry extends ConfigToDataConverter<HullConfig, HullData> {
    public static final HullRegistry INSTANCE = new HullRegistry();

    public HullRegistry() {
        super("module/hull", HullConfig.class, HullConfig::name, HullData::new);
    }
}