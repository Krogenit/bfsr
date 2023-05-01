package net.bfsr.config.component.crew;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;

@ConfigConverter
public class CrewRegistry extends ConfigToDataConverter<CrewConfig, CrewData> {
    public static final CrewRegistry INSTANCE = new CrewRegistry();

    public CrewRegistry() {
        super("module/crew", CrewConfig.class, CrewConfig::name, CrewData::new);
    }
}