package net.bfsr.event.entity.ship;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.Ship;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class ShipHullDamageByCollisionEvent extends Event {
    private final Ship ship;
    private final float contactX;
    private final float contactY;
    private final float normalX;
    private final float normalY;
}