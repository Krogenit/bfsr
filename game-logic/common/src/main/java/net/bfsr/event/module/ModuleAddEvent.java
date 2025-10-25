package net.bfsr.event.module;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Module;

@Getter
@AllArgsConstructor
public class ModuleAddEvent extends Event {
    private final Ship ship;
    private final Module module;
}
