package net.bfsr.config.component.engine;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class EngineRegistry extends ConfigToDataConverter<EngineConfig, EnginesData> {
    public static final EngineRegistry INSTANCE = new EngineRegistry();

    private EngineRegistry() {
        super("module/engine", EngineConfig.class, (fileName, engineConfig) -> fileName, EnginesData::new);
    }
}