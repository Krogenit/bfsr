package net.bfsr.entity.ship.module.weapon;

public enum WeaponType {
    GUN, BEAM;

    private static final WeaponType[] VALUES = values();

    public static WeaponType get(byte index) {
        return VALUES[index];
    }
}