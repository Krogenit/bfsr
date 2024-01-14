package net.bfsr.faction;

public enum Faction {
    HUMAN, SAIMON, ENGI;

    private static final Faction[] VALUES = values();

    public static Faction get(byte index) {
        return VALUES[index];
    }
}