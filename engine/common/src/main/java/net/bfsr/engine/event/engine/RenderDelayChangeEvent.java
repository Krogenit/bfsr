package net.bfsr.engine.event.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;

@Getter
@RequiredArgsConstructor
public class RenderDelayChangeEvent extends Event {
    private final int renderDelayFrames;
}
