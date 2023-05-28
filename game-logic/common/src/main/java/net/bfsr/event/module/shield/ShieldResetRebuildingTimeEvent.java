package net.bfsr.event.module.shield;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.component.shield.Shield;
import net.bfsr.engine.event.Event;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class ShieldResetRebuildingTimeEvent extends Event {
    private final Shield shield;
}