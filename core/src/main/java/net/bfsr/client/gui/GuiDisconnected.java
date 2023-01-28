package net.bfsr.client.gui;

import net.bfsr.client.gui.button.Button;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.string.StaticString;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.Transformation;
import org.joml.Vector2f;

public class GuiDisconnected extends Gui {
    private final String errorMessage;
    private final String description;
    private final StringObject text;
    private final StringObject textDescription;
    private final Gui prevGui;
    private final TextureObject background;

    public GuiDisconnected(Gui prevGui, String errorMessage, String description) {
        this.errorMessage = errorMessage;
        this.prevGui = prevGui;
        this.description = description;
        this.background = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiAdd));
        this.text = new StaticString(FontType.XOLONIUM, errorMessage);
        this.textDescription = new StaticString(FontType.XOLONIUM, description);
    }

    @Override
    protected void initElements() {
        Vector2f scale = new Vector2f(0.6f, 0.6f);

        background.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x, center.y)));
        background.setScale(new Vector2f(600 * scale.x, 278 * scale.y));

        Button button = new Button(TextureRegister.guiButtonBase, center.x, center.y + 84, 180, 30, "gui.ok", 14);
        button.setOnMouseClickedRunnable(() -> Core.getCore().setCurrentGui(prevGui));
        registerGuiObject(button);

        text.setFontSize(16);
        text.setPosition((int) (center.x - 286 * scale.x), (int) (center.y - 128 * scale.y));
        text.compile();

        textDescription.setFontSize(16);
        textDescription.setPosition((int) (center.x - 286 * scale.x), (int) (center.y - 74 * scale.y));
        textDescription.compile();
    }

    @Override
    public void render(BaseShader shader) {
        background.render(shader);
        super.render(shader);
    }

    @Override
    public void clear() {
        super.clear();
        text.clear();
        textDescription.clear();
    }

}
