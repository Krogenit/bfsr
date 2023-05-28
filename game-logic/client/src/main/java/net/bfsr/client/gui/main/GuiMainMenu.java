package net.bfsr.client.gui.main;

import net.bfsr.client.Core;
import net.bfsr.client.gui.connect.GuiConnect;
import net.bfsr.client.gui.settings.GuiSettings;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.object.TexturedGuiObject;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class GuiMainMenu extends Gui {
    @Override
    protected void initElements() {
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiLogoBFSR, 180, 180).atCenter(-90, -240));
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiBfsrText2, 690, 79).atCenter(-345, -189));

        int buttonWidth = 260;
        int buttonHeight = 40;
        int x = -buttonWidth / 2;
        registerGuiObject(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.singleplayer"),
                () -> Core.get().startSinglePlayer()).atCenter(x, -45));
        registerGuiObject(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.multiplayer"),
                () -> Core.get().openGui(new GuiConnect(this))).atCenter(x, 0));
        registerGuiObject(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.options"),
                () -> Core.get().openGui(new GuiSettings(this))).atCenter(x, 45));
        registerGuiObject(
                new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.quit"), () -> Core.get().stop()).atCenter(x,
                        90));
    }
}