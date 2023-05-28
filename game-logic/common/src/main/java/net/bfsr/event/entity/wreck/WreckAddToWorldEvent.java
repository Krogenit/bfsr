package net.bfsr.event.entity.wreck;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.wreck.Wreck;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class WreckAddToWorldEvent extends Event {
    private final Wreck wreck;
}