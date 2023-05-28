package net.bfsr.event.entity.ship;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class ShipNewMoveDirectionEvent extends Event {
    private final Ship ship;
    private final Direction direction;
}