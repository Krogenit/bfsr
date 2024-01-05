package net.bfsr.event.module.weapon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;

@AllArgsConstructor
@Getter
public class WeaponSlotRemovedEvent extends Event {
    private final WeaponSlot weaponSlot;
}