package net.bfsr.event.module.weapon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class WeaponShotEvent extends Event {
    private final WeaponSlot weaponSlot;
}