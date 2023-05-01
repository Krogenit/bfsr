package net.bfsr.config.component.engine;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class EngineRegistry extends ConfigToDataConverter<EngineConfig, EngineData> {
    public static final EngineRegistry INSTANCE = new EngineRegistry();

    public EngineRegistry() {
        super("module/engine", EngineConfig.class, EngineConfig::name, EngineData::new);
    }
}