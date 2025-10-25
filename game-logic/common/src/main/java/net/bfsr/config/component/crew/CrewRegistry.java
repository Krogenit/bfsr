package net.bfsr.config.component.crew;

import net.bfsr.engine.config.ConfigConverter;
import net.bfsr.engine.config.ConfigToDataConverter;

@ConfigConverter
public final class CrewRegistry extends ConfigToDataConverter<CrewConfig, CrewData> {
    public CrewRegistry() {
        super("module/crew", CrewConfig.class, (fileName, crewConfig) -> fileName, CrewData::new);
    }
}