package net.bfsr.client.launch;

import net.bfsr.client.Core;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.editor.hud.GuiInGameEditor;

public class EditorMain extends LWJGL3Main {
    @Override
    protected void init() {
        super.init();
        Core.get().startSinglePlayer();
    }

    @Override
    protected GuiInGame getGuiInGame() {
        return new GuiInGameEditor();
    }

    public static void main(String[] args) {
        new EditorMain().run();
    }
}