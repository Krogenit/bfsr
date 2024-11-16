package net.bfsr.entity.ship.module.weapon;

import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunRegistry;

public final class WeaponFactory {
    public static WeaponSlot createBeam(String name, BeamRegistry beamRegistry) {
        return new WeaponSlotBeam(beamRegistry.get(name));
    }

    public static WeaponSlot createGun(String name, GunRegistry gunRegistry) {
        return new WeaponSlot(gunRegistry.get(name));
    }
}