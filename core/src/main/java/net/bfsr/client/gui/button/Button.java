package net.bfsr.client.gui.button;

import lombok.Setter;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.StringCache;
import net.bfsr.client.render.font.StringOffsetType;
import net.bfsr.client.render.font.string.DynamicString;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.core.Core;
import net.bfsr.util.RunnableUtils;

public class Button extends TexturedGuiObject {
    private final StringObject stringObject;
    private final SoundRegistry collideSound;
    private final SoundRegistry clickSound;
    private boolean collided;
    @Setter
    private Runnable onMouseClickedRunnable;

    private Button(TextureRegister texture, int x, int y, int width, int height, String string, int fontSize, Runnable onMouseClickedRunnable) {
        super(texture, x, y, width, height);
        FontType font = FontType.XOLONIUM;
        StringCache stringCache = font.getStringCache();
        stringObject = new DynamicString(font, string, fontSize, StringOffsetType.CENTERED);
        stringObject.setPosition(x + width / 2, (int) (y + (height - stringCache.getHeight(string, fontSize)) / 2.0f + stringCache.getAscent(string, fontSize)));
        stringObject.compile();
        clickSound = SoundRegistry.buttonClick;
        collideSound = SoundRegistry.buttonCollide;
        this.onMouseClickedRunnable = onMouseClickedRunnable;
    }

    public Button(String string, int fontSize, Runnable onMouseClickedRunnable) {
        this(TextureRegister.guiButtonBase, 0, 0, 300, 50, string, fontSize, onMouseClickedRunnable);
    }

    public Button(int x, int y, String string, Runnable onMouseClickedRunnable) {
        this(TextureRegister.guiButtonBase, x, y, 300, 50, string, 20, onMouseClickedRunnable);
    }

    public Button(TextureRegister texture, int x, int y, int width, int height, String string, int fontSize) {
        this(texture, x, y, width, height, string, fontSize, RunnableUtils.EMPTY_RUNNABLE);
    }

    private Button(TextureRegister texture, int x, int y, int width, int height, String string) {
        this(texture, x, y, width, height, string, 18);
    }

    public Button(TextureRegister texture, int x, int y, int width, int height) {
        this(texture, x, y, width, height, "");
    }

    public Button(String string, Runnable onMouseClickedRunnable) {
        this(TextureRegister.guiButtonBase, 0, 0, 300, 50, string, 20, onMouseClickedRunnable);
    }

    public Button(TextureRegister texture, Runnable onMouseClickedRunnable) {
        this(texture, 0, 0, 300, 50, "", 20, onMouseClickedRunnable);
    }

    @Override
    public void update() {
        if (collideSound != null) {
            if (isIntersects()) {
                if (!collided) {
                    collided = true;
                    Core.getCore().getSoundManager().play(new GuiSoundSource(collideSound));
                }
            } else {
                collided = false;
            }
        }
    }

    @Override
    public void onMouseLeftClick() {
        if (isIntersects()) {
            onMouseClickedRunnable.run();

            if (clickSound != null) {
                Core.getCore().getSoundManager().play(new GuiSoundSource(clickSound));
            }
        }
    }

    @Override
    public void onMouseRightClick() {
        if (isIntersects() && clickSound != null) {
            Core.getCore().getSoundManager().play(new GuiSoundSource(clickSound));
        }
    }

    @Override
    public void render(BaseShader shader) {
        super.render(shader);
        stringObject.render();
        shader.enable();
    }

    @Override
    public Button setPosition(int x, int y) {
        super.setPosition(x, y);
        StringCache stringCache = stringObject.getStringCache();
        stringObject.setPosition(x + width / 2, (int) (y + (height - stringCache.getHeight(stringObject.getString(), stringObject.getFontSize())) / 2.0f +
                stringCache.getAscent(stringObject.getString(), stringObject.getFontSize())));
        return this;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        stringObject.setX(x + width / 2);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        StringCache stringCache = stringObject.getStringCache();
        stringObject.setY((int) (y + (height - stringCache.getHeight(stringObject.getString(), stringObject.getFontSize())) / 2.0f + stringCache.getAscent(stringObject.getString(),
                stringObject.getFontSize())));
    }

    public void setTextColor(float r, float g, float b, float a) {
        stringObject.setColor(r, g, b, a);
    }

    public void setString(String string) {
        stringObject.update(string);
    }

    @Override
    public void clear() {
        stringObject.clear();
    }
}
