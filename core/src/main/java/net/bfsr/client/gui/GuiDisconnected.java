package net.bfsr.client.gui;

import net.bfsr.client.font.FontRegistry;
import net.bfsr.client.font.GUIText;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.Transformation;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GuiDisconnected extends Gui {

    private final String errorMessage;
    private final String description;
    private final GUIText text;
    private final GUIText textDescription;
    private final Gui prevGui;
    private final TextureObject background;

    public GuiDisconnected(Gui prevGui, String errorMessage, String description) {
        this.errorMessage = errorMessage;
        this.prevGui = prevGui;
        this.description = description;
        this.background = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiAdd));
        this.text = new GUIText(errorMessage, new Vector2f(1, 1), FontRegistry.XOLONIUM,
                Transformation.getOffsetByScale(new Vector2f(center.x - 286, center.y - 128)), new Vector4f(1, 1, 1, 1), false, EnumParticlePositionType.Gui);
        this.textDescription = new GUIText(description, new Vector3f(1, 1, 1.2f), FontRegistry.CONSOLA,
                Transformation.getOffsetByScale(new Vector2f(center.x - 286, center.y - 74)), new Vector4f(1, 1, 1, 1), 0.415f, false, EnumParticlePositionType.Gui);
    }

    @Override
    public void init() {
        super.init();

        Vector2f scale = new Vector2f(0.6f, 0.6f);

        background.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x, center.y)));
        background.setScale(new Vector2f(600 * scale.x, 278 * scale.y));

        Button button = new Button(0, TextureRegister.guiButtonBase, new Vector2f(center.x, center.y + 140 * scale.y),
                new Vector2f(300 * scale.x, 50 * scale.y), "gui.ok", new Vector2f(1f * scale.x, 0.9f * scale.y));
        button.setOnMouseClickedRunnable(() -> Core.getCore().setCurrentGui(prevGui));
        buttons.add(button);

        text.setFontSize(Transformation.getScale(new Vector2f(1f * scale.x, 0.9f * scale.y)));
        text.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x - 286 * scale.x, center.y - 128 * scale.y)));
        text.updateText(errorMessage);

        textDescription.setFontSize(Transformation.getScale(new Vector2f(1f * scale.x, 0.9f * scale.y)));
        textDescription.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x - 286 * scale.x, center.y - 74 * scale.y)));
        textDescription.updateText(description);
    }

    @Override
    public void render(BaseShader shader) {
        OpenGLHelper.alphaGreater(0.01f);
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
