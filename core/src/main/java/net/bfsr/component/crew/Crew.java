package net.bfsr.component.crew;

public class Crew {
    private int crewSize, maxCrewSize;

    public Crew(int maxCrewSize) {
        this.maxCrewSize = maxCrewSize;
    }

    public void setCrewSize(int crewSize) {
        this.crewSize = crewSize;
    }

    public void setMaxCrewSize(int maxCrewSize) {
        this.maxCrewSize = maxCrewSize;
    }

    public int getCrewSize() {
        return crewSize;
    }

    public int getMaxCrewSize() {
        return maxCrewSize;
    }

    public float getCrewRegen() {
        return crewSize / 100f;
    }
}
