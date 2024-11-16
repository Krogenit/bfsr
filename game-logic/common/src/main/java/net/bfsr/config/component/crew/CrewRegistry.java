package net.bfsr.config.component.crew;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public final class CrewRegistry extends ConfigToDataConverter<CrewConfig, CrewData> {
    public CrewRegistry() {
        super("module/crew", CrewConfig.class, (fileName, crewConfig) -> fileName, CrewData::new);
    }
}