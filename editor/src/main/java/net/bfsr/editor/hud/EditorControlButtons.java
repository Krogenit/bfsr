package net.bfsr.editor.hud;

import net.bfsr.client.Core;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.particle.GuiParticleEditor;
import net.bfsr.editor.gui.ship.GuiShipEditor;
import net.bfsr.engine.gui.component.Button;

public class EditorControlButtons {
    public void init(HUD gui) {
        int buttonWidth = 240;
        int buttonHeight = 36;
        int y = 0;
        gui.registerGuiObject(EditorTheme.setupButtonColors(new Button(null, buttonWidth, buttonHeight, "Particle Editor", 22,
                () -> Core.get().openGui(new GuiParticleEditor())) {
            @Override
            public void updateMouseHover() {
                if (Core.get().getGuiManager().noGui()) {
                    super.updateMouseHover();
                }
            }
        }).atRight(-buttonWidth, y));
        y += buttonHeight;
        gui.registerGuiObject(EditorTheme.setupButtonColors(new Button(null, buttonWidth, buttonHeight, "Ship Editor", 22,
                () -> Core.get().openGui(new GuiShipEditor())) {
            @Override
            public void updateMouseHover() {
                if (Core.get().getGuiManager().noGui()) {
                    super.updateMouseHover();
                }
            }
        }).atRight(-buttonWidth, y));
    }
}