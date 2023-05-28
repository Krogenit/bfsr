package net.bfsr.event.entity.wreck;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.wreck.ShipWreck;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class BulletDamageShipWreckEvent extends Event {
    private final ShipWreck wreck;
    private final Bullet bullet;
    private final float contactX;
    private final float contactY;
    private final float normalX;
    private final float normalY;
}