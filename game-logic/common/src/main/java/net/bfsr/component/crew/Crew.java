package net.bfsr.component.crew;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.crew.CrewData;

@Getter
public class Crew {
    private final int maxCrewSize;
    @Setter
    private int crewSize;

    public Crew(CrewData crewData) {
        this.maxCrewSize = crewData.getMaxCapacity();
    }

    public float getCrewRegen() {
        return crewSize / 100.0f;
    }
}