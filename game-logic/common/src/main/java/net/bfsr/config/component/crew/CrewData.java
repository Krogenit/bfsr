package net.bfsr.config.component.crew;

import lombok.Getter;
import net.bfsr.config.ConfigData;

@Getter
public class CrewData extends ConfigData {
    private final int maxCapacity;

    CrewData(CrewConfig crewConfig, String fileName, int id, int registryId) {
        super(fileName, id, registryId);
        this.maxCapacity = crewConfig.maxCapacity();
    }
}