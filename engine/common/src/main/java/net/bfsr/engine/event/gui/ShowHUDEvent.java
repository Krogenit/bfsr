package net.bfsr.engine.event.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.gui.hud.HUDAdapter;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class ShowHUDEvent extends Event {
    private final HUDAdapter hud;
}