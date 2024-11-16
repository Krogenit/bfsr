package net.bfsr.editor.hud;

import net.bfsr.client.Client;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.particle.GuiParticleEditor;
import net.bfsr.editor.gui.ship.GuiShipEditor;
import net.bfsr.engine.gui.component.Button;

public class EditorHUD extends HUD {
    public EditorHUD() {
        int buttonWidth = 240;
        int buttonHeight = 36;
        int y = 0;
        add(EditorTheme.setupButton(new Button(buttonWidth, buttonHeight, "Particle Editor", 22,
                () -> Client.get().openGui(new GuiParticleEditor()))).atRight(-buttonWidth, y));
        y += buttonHeight;
        add(EditorTheme.setupButton(new Button(buttonWidth, buttonHeight, "Ship Editor", 22,
                () -> Client.get().openGui(new GuiShipEditor()))).atRight(-buttonWidth, y));
    }
}