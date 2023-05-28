package net.bfsr.client.gui.ingame;

import net.bfsr.client.Core;
import net.bfsr.client.gui.settings.GuiSettings;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.object.TexturedGuiObject;
import net.bfsr.engine.renderer.texture.TextureRegister;

import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

public class GuiInGameMenu extends Gui {
    private boolean wantCloseGui;

    @Override
    protected void initElements() {
        int x = -150;
        registerGuiObject(new Button(Lang.getString("gui.ingamemenu.backtogame"), () -> Core.get().closeGui()).atCenter(x, -30));
        registerGuiObject(new Button(Lang.getString("gui.ingamemenu.settings"),
                () -> Core.get().openGui(new GuiSettings(this))).atCenter(x, 30));
        registerGuiObject(
                new Button(Lang.getString("gui.ingamemenu.tomainmenu"), () -> Core.get().quitToMainMenu()).atCenter(x, 180));
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiLogoBFSR).atCenter(-128, -328).setSize(256, 256));
    }

    @Override
    public void update() {
        super.update();

        if (wantCloseGui) {
            Core.get().closeGui();
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

    @Override
    public void render() {
        guiRenderer.add(0, 0, width, height, 0.0f, 0.0f, 0.0f, 0.5f);
        super.render();
    }
}