package net.bfsr.event.entity.bullet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;

@Getter
@RequiredArgsConstructor
public class BulletDamageShipArmorEvent extends Event {
    private final Bullet bullet;
    private final Ship ship;
    private final float contactX;
    private final float contactY;
    private final float normalX;
    private final float normalY;
}