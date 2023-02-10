package net.bfsr.client.gui.menu;

import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiSettings;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.multiplayer.GuiConnect;
import net.bfsr.client.language.Lang;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.core.Core;

public class GuiMainMenu extends Gui {
    @Override
    protected void initElements() {
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiLogoBFSR).atCenter(-90, -240).setSize(180, 180));
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiBfsrText2).atCenter(-345, -189).setSize(690, 79));

        int buttonWidth = 260;
        int buttonHeight = 40;
        int x = -buttonWidth / 2;
        registerGuiObject(new Button(Lang.getString("gui.mainmenu.singleplayer"), () -> Core.get().startSinglePlayer()).atCenter(x, -45).setSize(buttonWidth, buttonHeight));
        registerGuiObject(new Button(Lang.getString("gui.mainmenu.multiplayer"), () -> Core.get().setCurrentGui(new GuiConnect(this))).atCenter(x, 0).setSize(260, 40));
        registerGuiObject(new Button(Lang.getString("gui.mainmenu.options"), () -> Core.get().setCurrentGui(new GuiSettings(this))).atCenter(x, 45).setSize(260, 40));
        registerGuiObject(new Button(Lang.getString("gui.mainmenu.quit"), () -> Core.get().stop()).atCenter(x, 90).setSize(260, 40));
    }
}