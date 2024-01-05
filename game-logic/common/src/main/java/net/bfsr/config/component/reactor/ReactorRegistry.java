package net.bfsr.config.component.reactor;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class ReactorRegistry extends ConfigToDataConverter<ReactorConfig, ReactorData> {
    public static final ReactorRegistry INSTANCE = new ReactorRegistry();

    private ReactorRegistry() {
        super("module/reactor", ReactorConfig.class, (fileName, wreckConfig) -> fileName, ReactorData::new);
    }
}