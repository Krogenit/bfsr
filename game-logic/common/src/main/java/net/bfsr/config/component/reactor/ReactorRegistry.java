package net.bfsr.config.component.reactor;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class ReactorRegistry extends ConfigToDataConverter<ReactorConfig, ReactorData> {
    public ReactorRegistry() {
        super("module/reactor", ReactorConfig.class, (fileName, wreckConfig) -> fileName, ReactorData::new);
    }
}