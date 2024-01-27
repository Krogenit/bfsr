package net.bfsr.event.module.weapon.beam;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class BeamDamageShipHullEvent extends Event {
    private final WeaponSlotBeam slot;
    private final Ship ship;
    private final float hitX, hitY;
    private final float normalX, normalY;
}