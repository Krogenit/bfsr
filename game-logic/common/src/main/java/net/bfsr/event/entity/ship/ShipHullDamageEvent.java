package net.bfsr.event.entity.ship;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.hull.HullCell;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class ShipHullDamageEvent extends Event {
    private final Ship ship;
    private final float contactX;
    private final float contactY;
    private final HullCell cell;
}