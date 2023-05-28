package net.bfsr.event.entity.wreck;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.wreck.ShipWreck;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class ShipWreckAddToWorldEvent extends Event {
    private final ShipWreck wreck;
}