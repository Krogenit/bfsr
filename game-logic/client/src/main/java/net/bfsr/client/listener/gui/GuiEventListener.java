package net.bfsr.client.listener.gui;

import net.bfsr.client.Core;
import net.bfsr.client.event.PlayerJoinGameEvent;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.gui.GuiManager;

public class GuiEventListener {
    private final Core core = Core.get();
    private final GuiManager guiManager = core.getGuiManager();

    @EventHandler
    public EventListener<PlayerJoinGameEvent> playerJoinGameEvent() {
        return event -> guiManager.showHUD(Core.get().createHUD());
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> guiManager.closeHUD();
    }
}
