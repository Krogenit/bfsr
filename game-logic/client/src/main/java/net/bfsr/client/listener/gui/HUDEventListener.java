package net.bfsr.client.listener.gui;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.event.gui.SelectSecondaryShipEvent;
import net.bfsr.client.event.gui.SelectShipEvent;
import net.bfsr.client.event.player.ShipControlStartedEvent;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;

@RequiredArgsConstructor
public class HUDEventListener {
    private final HUD hud;

    @EventHandler
    public EventListener<SelectShipEvent> selectShip() {
        return event -> hud.selectShip(event.getShip());
    }

    @EventHandler
    public EventListener<SelectSecondaryShipEvent> selectSecondaryShip() {
        return event -> hud.selectShipSecondary(event.getShip());
    }

    @EventHandler
    public EventListener<ShipControlStartedEvent> shipControlStarted() {
        return event -> hud.onShipControlStarted();
    }
}
