package net.bfsr.event.module.shield;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.module.shield.Shield;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class ShieldRebuildEvent extends Event {
    private final Shield shield;
}