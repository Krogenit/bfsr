package net.bfsr.editor.hud;

import net.bfsr.client.gui.hud.HUD;

public class GuiInGameEditor extends HUD {
    private final EditorControlButtons editorControlButtons = new EditorControlButtons();

    @Override
    protected void initElements() {
        super.initElements();
        editorControlButtons.init(this);
    }
}