package net.bfsr.client.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;

@Getter
@RequiredArgsConstructor
public class PingEvent extends Event {
    private final float ping;
}
