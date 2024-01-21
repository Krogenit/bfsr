package net.bfsr.event.entity.ship;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.Ship;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class ShipJumpInEvent extends Event {
    private final Ship ship;
}