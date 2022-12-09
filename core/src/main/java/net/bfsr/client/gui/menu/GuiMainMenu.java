package net.bfsr.client.gui.menu;

import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiSettings;
import net.bfsr.client.gui.GuiTextureObject;
import net.bfsr.client.gui.button.ButtonBase;
import net.bfsr.client.gui.multiplayer.GuiConnect;
import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import org.joml.Vector2f;

public class GuiMainMenu extends Gui {
    private final TextureObject logoText, logo;

    public GuiMainMenu() {
        logoText = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiBfsrText2));
        logo = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiLogoBFSR));
    }

    @Override
    public void init() {
        super.init();

        logo.setPosition(center.x, center.y - 150);
        logo.setScale(180.0f, 180.0f);

        logoText.setPosition(center.x, center.y - 150);
        logoText.setScale(1553.0f / 2.25f, 158.0f / 2.0f);

        ButtonBase button = new ButtonBase(0, new Vector2f(center.x, center.y - 45), new Vector2f(260, 40), "gui.mainmenu.singleplayer", new Vector2f(0.9f, 0.8f));
        button.setOnMouseClickedRunnable(() -> {
            Core.getCore().startSingleplayer();
            Core.getCore().setCurrentGui(null);
        });
        buttons.add(button);
        button = new ButtonBase(1, new Vector2f(center.x, center.y), new Vector2f(260, 40), "gui.mainmenu.multiplayer", new Vector2f(0.9f, 0.8f));
        button.setOnMouseClickedRunnable(() -> Core.getCore().setCurrentGui(new GuiConnect(this)));
        buttons.add(button);
        button = new ButtonBase(2, new Vector2f(center.x, center.y + 45), new Vector2f(260, 40), "gui.mainmenu.options", new Vector2f(0.9f, 0.8f));
        button.setOnMouseClickedRunnable(() -> Core.getCore().setCurrentGui(new GuiSettings(this)));
        buttons.add(button);
        button = new ButtonBase(3, new Vector2f(center.x, center.y + 90), new Vector2f(260, 40), "gui.mainmenu.quit", new Vector2f(0.9f, 0.8f));
        button.setOnMouseClickedRunnable(() -> Core.getCore().stop());
        buttons.add(button);
    }

    @Override
    public void render(BaseShader shader) {
        shader.enable();
        logo.render(shader);
        logoText.render(shader);
        super.render(shader);
    }
}
