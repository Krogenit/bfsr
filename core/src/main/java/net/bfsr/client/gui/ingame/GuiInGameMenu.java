package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiSettings;
import net.bfsr.client.gui.GuiTextureObject;
import net.bfsr.client.gui.button.ButtonBase;
import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.Transformation;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

public class GuiInGameMenu extends Gui {
    private final TextureObject logoBFSR;

    public GuiInGameMenu() {
        logoBFSR = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiLogoBFSR));
    }

    @Override
    public void init() {
        super.init();

        ButtonBase button = new ButtonBase(0, new Vector2f(center.x, center.y - 30), "gui.ingamemenu.backtogame");
        button.setTextColor(1, 1, 1, 1);
        button.setOnMouseClickedRunnable(() -> Core.getCore().setCurrentGui(null));
        buttons.add(button);
        button = new ButtonBase(1, new Vector2f(center.x, center.y + 30), "gui.ingamemenu.settings");
        button.setOnMouseClickedRunnable(() -> Core.getCore().setCurrentGui(new GuiSettings(this)));
        button.setTextColor(1, 1, 1, 1);
        buttons.add(button);
        button = new ButtonBase(2, new Vector2f(center.x, center.y + 180), "gui.ingamemenu.tomainmenu");
        button.setOnMouseClickedRunnable(() -> Core.getCore().quitToMainMenu());
        button.setTextColor(1, 1, 1, 1);
        buttons.add(button);
        logoBFSR.setPosition(center.x, center.y - 200);
        logoBFSR.setScale(256, 256);
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            Core.getCore().setCurrentGui(null);
        }
    }

    @Override
    public void render(BaseShader shader) {
        shader.setColor(new Vector4f(0, 0, 0, 0.5f));
        shader.setModelViewMatrix(Transformation.getModelViewMatrixGui(width / 2.0F, height / 2.0f, 0, width, height));
        shader.disableTexture();
        Renderer.quad.render();

        logoBFSR.render(shader);
        super.render(shader);
    }
}
