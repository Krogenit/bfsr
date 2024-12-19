package net.bfsr.client.gui.main;

import net.bfsr.client.Client;
import net.bfsr.client.gui.connect.GuiConnect;
import net.bfsr.client.gui.settings.GuiSettings;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class GuiMainMenu extends Gui {
    public GuiMainMenu() {
        add(new TexturedRectangle(TextureRegister.guiLogoBFSR, 180, 180).atCenter(0, 150));
        add(new TexturedRectangle(TextureRegister.guiBfsrText2, 690, 79).atCenter(0, 150));

        int buttonWidth = 260;
        int buttonHeight = 40;
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.singleplayer"),
                () -> Client.get().startSinglePlayer()).atCenter(0, 5));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.multiplayer"),
                () -> Client.get().openGui(new GuiConnect(this))).atCenter(0, -40));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.options"),
                () -> Client.get().openGui(new GuiSettings(this))).atCenter(0, -85));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.quit"), () -> Client.get().stop())
                .atCenter(0, -130));
    }
}