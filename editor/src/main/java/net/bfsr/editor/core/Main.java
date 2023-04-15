package net.bfsr.editor.core;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.editor.hud.GuiInGameEditor;
import net.bfsr.editor.world.EditorWorld;

public class Main extends net.bfsr.client.core.Main {
    @Override
    protected void init() {
        super.init();
        Core.get().setWorldSupplier(EditorWorld::new);
        Core.get().startSinglePlayer();
    }

    @Override
    protected GuiInGame getGuiInGame() {
        return new GuiInGameEditor();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}