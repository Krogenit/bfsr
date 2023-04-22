package net.bfsr.server.component.weapon;


import net.bfsr.config.weapon.beam.BeamRegistry;
import net.bfsr.config.weapon.gun.GunRegistry;

public final class WeaponBuilder {
    public static WeaponSlot createBeam(String name) {
        return new WeaponSlotBeam(BeamRegistry.INSTANCE.get(name));
    }

    public static WeaponSlot createGun(String name) {
        return new WeaponSlot(GunRegistry.INSTANCE.get(name));
    }
}