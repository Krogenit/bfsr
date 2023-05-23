package net.bfsr.config.component.reactor;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class ReactorRegistry extends ConfigToDataConverter<ReactorConfig, ReactorData> {
    public static final ReactorRegistry INSTANCE = new ReactorRegistry();

    public ReactorRegistry() {
        super("module/reactor", ReactorConfig.class, ReactorConfig::name, ReactorData::new);
    }
}