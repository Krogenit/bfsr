package net.bfsr.event.entity.bullet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class BulletDamageShipHullEvent extends Event {
    private final Bullet bullet;
    private final Ship ship;
    private final float contactX;
    private final float contactY;
}