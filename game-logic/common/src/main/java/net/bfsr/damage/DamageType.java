package net.bfsr.damage;

public enum DamageType {
    SHIELD, ARMOR, HULL;

    private static final DamageType[] VALUES = values();

    public static DamageType get(byte index) {
        return VALUES[index];
    }
}