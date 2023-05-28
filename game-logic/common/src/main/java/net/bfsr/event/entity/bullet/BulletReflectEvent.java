package net.bfsr.event.entity.bullet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.bullet.Bullet;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class BulletReflectEvent extends Event {
    private final Bullet bullet;
}