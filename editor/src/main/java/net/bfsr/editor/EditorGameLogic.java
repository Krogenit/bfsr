package net.bfsr.editor;

import net.bfsr.client.Core;
import net.bfsr.client.event.PlayerJoinGameEvent;
import net.bfsr.editor.hud.GuiInGameEditor;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;

@Listener
public class EditorGameLogic extends Core {
    @Override
    public void init() {
        super.init();
        eventBus.subscribe(this);
        startSinglePlayer();
    }

    @Handler(priority = -1)
    public void event(PlayerJoinGameEvent event) {
        getGuiManager().showHUD(new GuiInGameEditor());
    }
}