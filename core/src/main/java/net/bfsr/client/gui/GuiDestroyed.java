package net.bfsr.client.gui;

import net.bfsr.client.gui.button.Button;
import net.bfsr.client.language.Lang;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.string.StaticString;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.client.PacketRespawn;
import org.joml.Vector2f;

public class GuiDestroyed extends Gui {
    private final String errorMessage;
    private final String description;
    private final StringObject text;
    private final StringObject textDescription;
    private final TextureObject background;

    public GuiDestroyed(String destroyedBy) {
        this.errorMessage = "gui.destroyed.shipWasDestroyed";
        this.description = destroyedBy;
        this.background = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiAdd));
        this.text = new StaticString(FontType.XOLONIUM, Lang.getString(errorMessage));
        this.textDescription = new StaticString(FontType.CONSOLA, Lang.getString("gui.destroyed.destroyedBy") + ": " + description);
    }

    @Override
    protected void initElements() {
        Vector2f scale = new Vector2f(0.6f, 0.6f);

        background.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x, center.y)));
        background.setScale(new Vector2f(600 * scale.x, 278 * scale.y));

        Button button = new Button(TextureRegister.guiButtonBase, center.x + 96, center.y + 60, 150, 30, "gui.destroyed.respawn", 14);
        button.setOnMouseClickedRunnable(() -> {
            Vector2f position = Core.getCore().getRenderer().getCamera().getPosition();
            Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketRespawn(position.x, position.y));
            Core.getCore().setCurrentGui(null);
        });
        registerGuiObject(button);

        button = new Button(TextureRegister.guiButtonBase, center.x - 96, center.y + 60, 150, 30, "gui.ingamemenu.tomainmenu", 14);
        button.setOnMouseClickedRunnable(() -> Core.getCore().quitToMainMenu());
        registerGuiObject(button);

        text.setFontSize(16);
        text.setPosition((int) (center.x - 286 * scale.x), (int) (center.y - 128 * scale.y));
        text.compile();
        textDescription.setFontSize(14);
        textDescription.setPosition((int) (center.x - 286 * scale.x), (int) (center.y - 74 * scale.y));
        textDescription.compile();
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
