package net.bfsr.client.gui.ingame;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiSettings;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.language.Lang;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.TextureRegister;
import org.lwjgl.glfw.GLFW;

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

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            wantCloseGui = true;
        }
    }

    @Override
    public void render(float interpolation) {
        SpriteRenderer.INSTANCE.addGUIElementToRenderPipeLine(0, 0, width, height, 0.0f, 0.0f, 0.0f, 0.5f, 0, BufferType.GUI);
        super.render(interpolation);
    }
}
