package net.bfsr.client.listener.gui;

import net.bfsr.client.Client;
import net.bfsr.client.event.PlayerJoinGameEvent;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.gui.GuiManager;

public class GuiEventListener {
    private final Client client = Client.get();
    private final GuiManager guiManager = client.getGuiManager();

    @EventHandler
    public EventListener<PlayerJoinGameEvent> playerJoinGameEvent() {
        return event -> guiManager.showHUD(Client.get().createHUD());
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> guiManager.closeHUD();
    }
}
