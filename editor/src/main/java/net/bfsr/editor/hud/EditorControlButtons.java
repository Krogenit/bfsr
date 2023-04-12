package net.bfsr.editor.hud;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.editor.gui.ColorScheme;
import net.bfsr.editor.gui.particle.GuiParticleEditor;

public class EditorControlButtons {
    public void init(GuiInGame gui) {
        int buttonWidth = 240;
        int buttonHeight = 36;
        gui.registerGuiObject(ColorScheme.setupButtonColors(new Button(null, buttonWidth, buttonHeight, "Particle Editor", 22, () -> Core.get().setCurrentGui(new GuiParticleEditor())) {
            @Override
            public void updateMouseHover() {
                if (Core.get().getCurrentGui() == null) {
                    super.updateMouseHover();
                }
            }
        }).atRight(-buttonWidth, 0));
    }
}