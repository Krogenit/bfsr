package net.bfsr.client.listener.gui;

import net.bfsr.client.event.PingEvent;
import net.bfsr.client.event.chat.ChatMessageEvent;
import net.bfsr.client.event.gui.SelectSecondaryShipEvent;
import net.bfsr.client.event.gui.SelectShipEvent;
import net.bfsr.client.event.player.ShipControlStartedEvent;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.gui.ShowHUDEvent;

public class HUDEventListener {
    private HUD hud;

    @EventHandler
    public EventListener<ShowHUDEvent> showHUD() {
        return event -> this.hud = (HUD) event.hud();
    }

    @EventHandler
    public EventListener<ChatMessageEvent> chatMessage() {
        return event -> hud.addChatMessage(event.getMessage());
    }

    @EventHandler
    public EventListener<SelectShipEvent> selectShip() {
        return event -> hud.selectShip(event.getShip());
    }

    @EventHandler
    public EventListener<SelectSecondaryShipEvent> selectSecondaryShip() {
        return event -> hud.selectShipSecondary(event.getShip());
    }

    @EventHandler
    public EventListener<PingEvent> ping() {
        return event -> hud.setPing(event.getPing());
    }

    @EventHandler
    public EventListener<ShipControlStartedEvent> shipControlStarted() {
        return event -> hud.onShipControlStarted();
    }
}
