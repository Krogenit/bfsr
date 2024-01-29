package net.bfsr.event.module.weapon.beam;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;

@Getter
@Setter
@Accessors(fluent = true)
public class BeamDamageShipArmorEvent extends Event {
    private WeaponSlotBeam slot;
    private Ship ship;
    private float hitX, hitY;
    private float normalX, normalY;

    public BeamDamageShipArmorEvent set(WeaponSlotBeam slot, Ship ship, float hitX, float hitY, float normalX, float normalY) {
        this.slot = slot;
        this.ship = ship;
        this.hitX = hitX;
        this.hitY = hitY;
        this.normalX = normalX;
        this.normalY = normalY;
        return this;
    }
}