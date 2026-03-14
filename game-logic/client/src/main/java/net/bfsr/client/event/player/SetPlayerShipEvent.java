package net.bfsr.client.event.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.ship.Ship;

@Getter
@RequiredArgsConstructor
public class SetPlayerShipEvent extends Event {
    private final Ship oldShip;
    private final Ship ship;
}
