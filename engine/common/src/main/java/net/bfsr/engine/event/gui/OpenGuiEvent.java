package net.bfsr.engine.event.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.gui.Gui;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class OpenGuiEvent extends Event {
    private final Gui gui;
}