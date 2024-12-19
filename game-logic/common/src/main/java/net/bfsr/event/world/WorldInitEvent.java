package net.bfsr.event.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;
import net.bfsr.world.World;

@Getter
@RequiredArgsConstructor
public class WorldInitEvent extends Event {
    private final World world;
}
