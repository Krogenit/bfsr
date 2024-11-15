package net.bfsr.config.component.engine;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class EngineRegistry extends ConfigToDataConverter<EngineConfig, EnginesData> {
    public EngineRegistry() {
        super("module/engine", EngineConfig.class, (fileName, engineConfig) -> fileName, EnginesData::new);
    }
}