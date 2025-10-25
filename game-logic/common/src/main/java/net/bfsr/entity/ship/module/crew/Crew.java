package net.bfsr.entity.ship.module.crew;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.crew.CrewData;
import net.bfsr.entity.ship.module.Module;
import net.bfsr.entity.ship.module.ModuleType;

@Getter
public class Crew extends Module {
    private final int maxCrewSize;
    @Setter
    private int crewSize;

    public Crew(CrewData crewData) {
        super(crewData);
        this.maxCrewSize = crewData.getMaxCapacity();
    }

    public float getCrewRegen() {
        return crewSize / 100.0f;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.CREW;
    }
}