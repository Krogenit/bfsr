package net.bfsr.client.listener.gui;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.event.gui.SelectShipEvent;
import net.bfsr.client.event.player.SetPlayerShipEvent;
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
    public EventListener<SetPlayerShipEvent> onPlayerShipSet() {
        return event -> hud.setPlayerShip(event.getShip());
    }
}
