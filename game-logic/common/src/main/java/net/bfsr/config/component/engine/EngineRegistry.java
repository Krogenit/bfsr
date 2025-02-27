package net.bfsr.config.component.engine;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class EngineRegistry extends ConfigToDataConverter<EngineConfig, EnginesData> {
    public EngineRegistry() {
        super("module/engine", EngineConfig.class, (fileName, engineConfig) -> fileName, EnginesData::new);
    }
}