package net.bfsr.event.module.weapon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.engine.event.Event;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class BeamShotEvent extends Event {
    private final WeaponSlotBeam weaponSlot;
}