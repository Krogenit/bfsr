package net.bfsr.editor.hud;

import net.bfsr.client.gui.ingame.GuiInGame;

public class GuiInGameEditor extends GuiInGame {
    private final EditorControlButtons editorControlButtons = new EditorControlButtons();

    @Override
    protected void initElements() {
        super.initElements();
        editorControlButtons.init(this);
    }
}