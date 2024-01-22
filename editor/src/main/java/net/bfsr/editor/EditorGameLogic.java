package net.bfsr.editor;

import net.bfsr.client.Core;
import net.bfsr.client.event.PlayerJoinGameEvent;
import net.bfsr.editor.hud.GuiInGameEditor;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.engio.mbassy.listener.Listener;

@Listener
public class EditorGameLogic extends Core {
    @Override
    public void init() {
        super.init();
        eventBus.register(this);
        startSinglePlayer();
    }

    @EventHandler
    public EventListener<PlayerJoinGameEvent> playerJoinGameEvent() {
        return event -> getGuiManager().showHUD(new GuiInGameEditor());
    }
}