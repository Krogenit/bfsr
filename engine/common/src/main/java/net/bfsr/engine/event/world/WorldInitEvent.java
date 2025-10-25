package net.bfsr.engine.event.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.world.World;

@Getter
@RequiredArgsConstructor
public class WorldInitEvent extends Event {
    private final World world;
}
