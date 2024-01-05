package net.bfsr.entity.ship.module.weapon;

import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunRegistry;

public final class WeaponFactory {
    public static WeaponSlot createBeam(String name) {
        return new WeaponSlotBeam(BeamRegistry.INSTANCE.get(name));
    }

    public static WeaponSlot createGun(String name) {
        return new WeaponSlot(GunRegistry.INSTANCE.get(name));
    }
}