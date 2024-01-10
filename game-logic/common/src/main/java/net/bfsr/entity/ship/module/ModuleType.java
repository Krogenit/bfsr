package net.bfsr.entity.ship.module;

public enum ModuleType {
    ENGINE, SHIELD, HULL, CREW, REACTOR, CARGO, WEAPON_SLOT, ARMOR;

    private static final ModuleType[] VALUES = values();

    public static ModuleType get(byte index) {
        return VALUES[index];
    }
}