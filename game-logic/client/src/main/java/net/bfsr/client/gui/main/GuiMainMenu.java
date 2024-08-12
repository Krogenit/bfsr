package net.bfsr.client.gui.main;

import net.bfsr.client.Core;
import net.bfsr.client.gui.connect.GuiConnect;
import net.bfsr.client.gui.settings.GuiSettings;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class GuiMainMenu extends Gui {
    public GuiMainMenu() {
        add(new TexturedRectangle(TextureRegister.guiLogoBFSR, 180, 180).atCenter(-90, -240));
        add(new TexturedRectangle(TextureRegister.guiBfsrText2, 690, 79).atCenter(-345, -189));

        int buttonWidth = 260;
        int buttonHeight = 40;
        int x = -buttonWidth / 2;
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.singleplayer"),
                () -> Core.get().startSinglePlayer()).atCenter(x, -45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.multiplayer"),
                () -> Core.get().openGui(new GuiConnect(this))).atCenter(x, 0));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.options"),
                () -> Core.get().openGui(new GuiSettings(this))).atCenter(x, 45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.quit"), () -> Core.get().stop())
                .atCenter(x, 90));
    }
}