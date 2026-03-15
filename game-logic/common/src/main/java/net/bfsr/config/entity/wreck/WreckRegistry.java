package net.bfsr.config.entity.wreck;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class WreckRegistry extends ConfigToDataConverter<WreckConfig, WreckData> {
    public WreckRegistry() {
        super("entity/wreck", WreckConfig.class, (fileName, wreckConfig) -> fileName,
                (config, fileName, index, registryId) -> new WreckData(config, index, registryId));
    }
}