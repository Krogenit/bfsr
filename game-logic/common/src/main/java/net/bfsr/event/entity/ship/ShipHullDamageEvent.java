package net.bfsr.event.entity.ship;

import net.bfsr.entity.ship.Ship;

public record ShipHullDamageEvent(Ship ship, float contactX, float contactY) {}