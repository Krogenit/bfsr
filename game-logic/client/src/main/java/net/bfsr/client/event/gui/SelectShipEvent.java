package net.bfsr.client.event.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.Ship;

@Getter
@RequiredArgsConstructor
public class SelectShipEvent extends Event {
    private final Ship ship;
}
