package net.bfsr.event.module.shield;

import net.bfsr.entity.ship.Ship;

public record ShieldDamageByCollision(Ship ship, float contactX, float contactY, float normalX, float normalY) {}