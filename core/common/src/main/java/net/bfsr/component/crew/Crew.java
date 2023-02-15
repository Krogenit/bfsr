package net.bfsr.component.crew;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Crew {
    private final int maxCrewSize;
    @Setter
    private int crewSize;

    public Crew(int maxCrewSize) {
        this.maxCrewSize = maxCrewSize;
    }

    public float getCrewRegen() {
        return crewSize / 100.0f;
    }
}
