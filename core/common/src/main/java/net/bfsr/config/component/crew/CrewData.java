package net.bfsr.config.component.crew;

import lombok.Getter;
import net.bfsr.config.ConfigData;

@Getter
public class CrewData extends ConfigData {
    private final int maxCapacity;

    public CrewData(CrewConfig crewConfig, int dataIndex) {
        super(crewConfig.name(), dataIndex);
        this.maxCapacity = crewConfig.maxCapacity();
    }
}