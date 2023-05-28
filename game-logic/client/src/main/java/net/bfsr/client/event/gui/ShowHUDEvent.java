package net.bfsr.client.event.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.client.gui.hud.HUDAdapter;
import net.bfsr.engine.event.Event;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class ShowHUDEvent extends Event {
    private final HUDAdapter hud;
}