package net.bfsr.client.gui.ingame;

import net.bfsr.client.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiSettings;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.texture.TextureRegister;

import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

public class GuiInGameMenu extends Gui {
    private boolean wantCloseGui;

    @Override
    protected void initElements() {
        int x = -150;
        registerGuiObject(new Button(Lang.getString("gui.ingamemenu.backtogame"), () -> Core.get().setCurrentGui(null)).atCenter(x, -30));
        registerGuiObject(new Button(Lang.getString("gui.ingamemenu.settings"), () -> Core.get().setCurrentGui(new GuiSettings(this))).atCenter(x, 30));
        registerGuiObject(new Button(Lang.getString("gui.ingamemenu.tomainmenu"), () -> Core.get().quitToMainMenu()).atCenter(x, 180));
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiLogoBFSR).atCenter(-128, -328).setSize(256, 256));
    }

    @Override
    public void update() {
        super.update();

        if (wantCloseGui) {
            Core.get().setCurrentGui(null);
            wantCloseGui = false;
        }
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == KEY_ESCAPE) {
            wantCloseGui = true;
        }
    }

    @Override
    public void render() {
        Engine.renderer.guiRenderer.add(0, 0, width, height, 0.0f, 0.0f, 0.0f, 0.5f);
        super.render();
    }
}