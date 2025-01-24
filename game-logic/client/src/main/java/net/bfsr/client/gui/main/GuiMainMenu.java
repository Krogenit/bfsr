package net.bfsr.client.gui.main;

import net.bfsr.client.Client;
import net.bfsr.client.gui.connect.GuiConnect;
import net.bfsr.client.gui.settings.GuiSettings;
import net.bfsr.client.language.LanguageManager;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class GuiMainMenu extends Gui {
    public GuiMainMenu() {
        add(new TexturedRectangle(TextureRegister.guiLogoBFSR, 180, 180).atCenter(0, 150));
        add(new TexturedRectangle(TextureRegister.guiBfsrText2, 690, 79).atCenter(0, 150));

        Client client = Client.get();
        LanguageManager languageManager = client.getLanguageManager();
        int buttonWidth = 260;
        int buttonHeight = 40;
        add(new Button(buttonWidth, buttonHeight, languageManager.getString("gui.mainmenu.singleplayer"),
                (mouseX, mouseY) -> client.startSinglePlayer()).atCenter(0, 5));
        add(new Button(buttonWidth, buttonHeight, languageManager.getString("gui.mainmenu.multiplayer"),
                (mouseX, mouseY) -> client.openGui(new GuiConnect(this))).atCenter(0, -40));
        add(new Button(buttonWidth, buttonHeight, languageManager.getString("gui.mainmenu.options"),
                (mouseX, mouseY) -> client.openGui(new GuiSettings(this))).atCenter(0, -85));
        add(new Button(buttonWidth, buttonHeight, languageManager.getString("gui.mainmenu.quit"), (mouseX, mouseY) -> client.shutdown())
                .atCenter(0, -130));
    }
}