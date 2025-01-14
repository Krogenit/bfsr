package net.bfsr.client.gui.ingame;

import net.bfsr.client.Client;
import net.bfsr.client.gui.settings.GuiSettings;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.texture.TextureRegister;

import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

public class GuiInGameMenu extends Gui {
    private boolean wantCloseGui;

    public GuiInGameMenu() {
        add(new Rectangle().atBottomLeft(0, 0).setFillParent().setAllColors(0.0f, 0.0f, 0.0f, 0.5f));
        add(new Button(Lang.getString("gui.ingamemenu.backtogame"), () -> Client.get().closeGui()).atCenter(0, -20));
        add(new Button(Lang.getString("gui.ingamemenu.settings"), () -> Client.get().openGui(new GuiSettings(this))).atCenter(0, -80));
        add(new Button(Lang.getString("gui.ingamemenu.tomainmenu"), () -> Client.get().quitToMainMenu()).atCenter(0, -230));
        add(new TexturedRectangle(TextureRegister.guiLogoBFSR).atCenter(0, 200).setSize(256, 256));
    }

    @Override
    public void update() {
        super.update();

        if (wantCloseGui) {
            Client.get().closeGui();
            wantCloseGui = false;
        }
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (key == KEY_ESCAPE) {
            wantCloseGui = true;
        }

        return input;
    }
}