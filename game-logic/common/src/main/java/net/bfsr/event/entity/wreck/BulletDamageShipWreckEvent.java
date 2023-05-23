package net.bfsr.event.entity.wreck;

import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.wreck.ShipWreck;

public record BulletDamageShipWreckEvent(
        ShipWreck wreck,
        Bullet bullet,
        float contactX,
        float contactY,
        float normalX,
        float normalY
) {}