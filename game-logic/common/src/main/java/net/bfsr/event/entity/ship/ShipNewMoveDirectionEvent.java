package net.bfsr.event.entity.ship;

import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;

public record ShipNewMoveDirectionEvent(Ship ship, Direction direction) {}