package net.bfsr.client.gui.main;

import net.bfsr.client.Client;
import net.bfsr.client.assets.TextureRegister;
import net.bfsr.client.gui.connect.GuiConnect;
import net.bfsr.client.gui.objects.SimpleButton;
import net.bfsr.client.gui.settings.GuiSettings;
import net.bfsr.client.language.LanguageManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;

public class GuiMainMenu extends Gui {
    public GuiMainMenu() {
        add(new TexturedRectangle(TextureRegister.guiLogoBFSR.getTextureData(), 180, 180).atCenter(0, 150));
        Label label = new Label(Engine.getFontManager().getDefaultFont(), "Spaceclipse", 48);
        label.setShadow(true);
        label.setShadowOffsetX(2);
        label.setShadowOffsetY(-2);
        add(label.atCenter(0, 150));

        Client client = Client.get();
        LanguageManager languageManager = client.getLanguageManager();
        int buttonWidth = 260;
        int buttonHeight = 40;
        add(new SimpleButton(buttonWidth, buttonHeight, languageManager.getString("gui.mainmenu.singleplayer"),
                (mouseX, mouseY) -> client.startSinglePlayer()).atCenter(0, 5));
        add(new SimpleButton(buttonWidth, buttonHeight, languageManager.getString("gui.mainmenu.multiplayer"),
                (mouseX, mouseY) -> client.openGui(new GuiConnect(this))).atCenter(0, -40));
        add(new SimpleButton(buttonWidth, buttonHeight, languageManager.getString("gui.mainmenu.options"),
                (mouseX, mouseY) -> client.openGui(new GuiSettings(this))).atCenter(0, -85));
        add(new SimpleButton(buttonWidth, buttonHeight, languageManager.getString("gui.mainmenu.quit"),
                (mouseX, mouseY) -> client.shutdown()).atCenter(0, -130));
    }
}