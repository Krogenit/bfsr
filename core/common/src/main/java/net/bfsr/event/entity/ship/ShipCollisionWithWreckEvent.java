package net.bfsr.event.entity.ship;

import net.bfsr.entity.ship.Ship;

public record ShipCollisionWithWreckEvent(Ship ship, float contactX, float contactY, float normalX, float normalY) {}