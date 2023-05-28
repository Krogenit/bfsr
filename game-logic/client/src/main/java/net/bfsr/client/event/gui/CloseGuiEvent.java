package net.bfsr.client.event.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.gui.Gui;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class CloseGuiEvent extends Event {
    private final Gui gui;
}