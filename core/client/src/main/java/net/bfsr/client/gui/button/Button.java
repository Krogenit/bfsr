package net.bfsr.client.gui.button;

import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringCache;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.util.RunnableUtils;

public class Button extends TexturedGuiObject {
    private final StringObject stringObject;
    private final SoundRegistry collideSound;
    private final SoundRegistry clickSound;
    private boolean collided;
    @Setter
    private Runnable onMouseClickedRunnable;

    public Button(TextureRegister texture, int x, int y, int width, int height, String string, int fontSize, Runnable onMouseClickedRunnable) {
        super(texture, x, y, width, height);
        FontType font = FontType.XOLONIUM;
        StringCache stringCache = font.getStringCache();
        this.stringObject = new StringObject(font, string, fontSize, StringOffsetType.CENTERED);
        this.stringObject.setPosition(x + width / 2, (int) (y + (height - stringCache.getHeight(string, fontSize)) / 2.0f + stringCache.getAscent(string, fontSize)));
        this.stringObject.compile();
        this.clickSound = SoundRegistry.buttonClick;
        this.collideSound = SoundRegistry.buttonCollide;
        this.onMouseClickedRunnable = onMouseClickedRunnable;
    }

    public Button(TextureRegister texture, int width, int height, String string, int fontSize, Runnable onMouseClickedRunnable) {
        this(texture, 0, 0, width, height, string, fontSize, onMouseClickedRunnable);
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
                    Core.get().getSoundManager().play(new GuiSoundSource(collideSound));
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
                Core.get().getSoundManager().play(new GuiSoundSource(clickSound));
            }
        }
    }

    @Override
    public void onMouseRightClick() {
        if (isIntersects() && clickSound != null) {
            Core.get().getSoundManager().play(new GuiSoundSource(clickSound));
        }
    }

    @Override
    public void render() {
        super.render();
        if (stringObject.getString().length() > 0) {
            stringObject.render();
        }
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
