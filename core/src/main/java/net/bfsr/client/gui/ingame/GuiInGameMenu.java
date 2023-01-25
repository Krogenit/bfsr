package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiSettings;
import net.bfsr.client.gui.GuiTextureObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.Transformation;
import org.lwjgl.glfw.GLFW;

public class GuiInGameMenu extends Gui {
    private final TextureObject logoBFSR;
    private boolean wantCloseGui;

    public GuiInGameMenu() {
        logoBFSR = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiLogoBFSR));
    }

    @Override
    protected void initElements() {
        Button button = new Button(center.x, center.y - 30, "gui.ingamemenu.backtogame", () -> Core.getCore().setCurrentGui(null));
        button.setTextColor(1, 1, 1, 1);
        registerGuiObject(button);
        button = new Button(center.x, center.y + 30, "gui.ingamemenu.settings", () -> Core.getCore().setCurrentGui(new GuiSettings(this)));
        button.setTextColor(1, 1, 1, 1);
        registerGuiObject(button);
        button = new Button(center.x, center.y + 180, "gui.ingamemenu.tomainmenu", () -> Core.getCore().quitToMainMenu());
        button.setTextColor(1, 1, 1, 1);
        registerGuiObject(button);
        logoBFSR.setPosition(center.x, center.y - 200);
        logoBFSR.setScale(256, 256);
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

        logoBFSR.render(shader);
        super.render(shader);
    }
}
