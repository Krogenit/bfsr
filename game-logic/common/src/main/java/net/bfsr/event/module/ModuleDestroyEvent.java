package net.bfsr.event.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.module.DamageableModule;

@Getter
@RequiredArgsConstructor
public class ModuleDestroyEvent extends Event {
    private final DamageableModule module;
}