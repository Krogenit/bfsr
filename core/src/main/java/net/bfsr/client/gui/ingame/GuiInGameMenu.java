package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiSettings;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.language.Lang;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import org.lwjgl.glfw.GLFW;

public class GuiInGameMenu extends Gui {
    private boolean wantCloseGui;

    @Override
    protected void initElements() {
        int x = -150;
        registerGuiObject(new Button(Lang.getString("gui.ingamemenu.backtogame"), () -> Core.getCore().setCurrentGui(null)).atCenter(x, -30));
        registerGuiObject(new Button(Lang.getString("gui.ingamemenu.settings"), () -> Core.getCore().setCurrentGui(new GuiSettings(this))).atCenter(x, 30));
        registerGuiObject(new Button(Lang.getString("gui.ingamemenu.tomainmenu"), () -> Core.getCore().quitToMainMenu()).atCenter(x, 180));
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiLogoBFSR).atCenter(-128, -328).setSize(256, 256));
    }

    @Override
    public void update() {
        super.update();

        if (wantCloseGui) {
            Core.getCore().setCurrentGui(null);
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
    public void render(BaseShader shader) {
        shader.setColor(0.0f, 0.0f, 0.0f, 0.5f);
        shader.setModelMatrix(Transformation.getModelViewMatrixGui(width / 2.0F, height / 2.0f, 0, width, height).get(ShaderProgram.MATRIX_BUFFER));
        shader.disableTexture();
        Renderer.centeredQuad.renderIndexed();

        super.render(shader);
    }
}
