package net.bfsr.client.gui.button;

import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.font.StringObject;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringCache;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.sound.SoundRegistry;
import net.bfsr.engine.sound.SoundSource;
import net.bfsr.util.RunnableUtils;
import org.joml.Vector4f;

public class Button extends TexturedGuiObject {
    private final StringObject stringObject;
    @Setter
    private SoundRegistry collideSound;
    @Setter
    private SoundRegistry clickSound;
    @Setter
    private Runnable onMouseClickRunnable;
    @Setter
    private Runnable onMouseRightClickRunnable = RunnableUtils.EMPTY_RUNNABLE;
    private int stringXOffset;
    private final int stringYOffset;

    public Button(TextureRegister texture, int x, int y, int width, int height, String string, FontType fontType, int fontSize, int stringXOffset, int stringYOffset,
                  StringOffsetType stringOffsetType, Runnable onMouseClickRunnable) {
        super(texture, x, y, width, height);
        StringCache stringCache = fontType.getStringCache();
        this.stringXOffset = stringXOffset;
        this.stringObject = new StringObject(fontType, string, x + stringXOffset, y + stringCache.getCenteredYOffset(string, height, fontSize) + stringYOffset, fontSize, stringOffsetType)
                .compile();
        this.onMouseClickRunnable = onMouseClickRunnable;
        this.stringYOffset = stringYOffset;
    }

    public Button(TextureRegister texture, int x, int y, int width, int height, String string, FontType fontType, int fontSize, int stringYOffset, Runnable onMouseClickRunnable) {
        this(texture, x, y, width, height, string, fontType, fontSize, width / 2, stringYOffset, StringOffsetType.CENTERED, onMouseClickRunnable);
    }

    public Button(TextureRegister texture, int x, int y, int width, int height, String string, FontType fontType, int fontSize, Runnable onMouseClickRunnable) {
        this(texture, x, y, width, height, string, fontType, fontSize, 0, onMouseClickRunnable);
    }

    public Button(TextureRegister texture, int x, int y, int width, int height, String string, int fontSize, Runnable onMouseClickRunnable) {
        this(texture, x, y, width, height, string, FontType.XOLONIUM, fontSize, onMouseClickRunnable);
    }

    public Button(TextureRegister texture, int width, int height, String string, int fontSize, Runnable onMouseClickRunnable) {
        this(texture, 0, 0, width, height, string, fontSize, onMouseClickRunnable);
    }

    public Button(TextureRegister texture, int width, int height, String string, FontType fontType, int fontSize, int stringYOffset, Runnable onMouseClickRunnable) {
        this(texture, 0, 0, width, height, string, fontType, fontSize, stringYOffset, onMouseClickRunnable);
    }

    public Button(int width, int height, String string, FontType fontType, int fontSize, int stringYOffset) {
        this(null, 0, 0, width, height, string, fontType, fontSize, stringYOffset, RunnableUtils.EMPTY_RUNNABLE);
    }

    public Button(int width, int height, String string, FontType fontType, int fontSize, int stringYOffset, Runnable onMouseClickRunnable) {
        this(null, 0, 0, width, height, string, fontType, fontSize, stringYOffset, onMouseClickRunnable);
    }

    public Button(String string, int fontSize, Runnable onMouseClickRunnable) {
        this(TextureRegister.guiButtonBase, 0, 0, 300, 50, string, fontSize, onMouseClickRunnable);
    }

    public Button(TextureRegister texture, int x, int y, int width, int height, String string, int fontSize) {
        this(texture, x, y, width, height, string, fontSize, RunnableUtils.EMPTY_RUNNABLE);
    }

    public Button(int width, int height, String string, Runnable onMouseClickRunnable) {
        this(TextureRegister.guiButtonBase, 0, 0, width, height, string, 20, onMouseClickRunnable);
    }

    public Button(String string, Runnable onMouseClickRunnable) {
        this(TextureRegister.guiButtonBase, 0, 0, 300, 50, string, 20, onMouseClickRunnable);
    }

    public Button(TextureRegister texture, Runnable onMouseClickRunnable) {
        this(texture, 0, 0, 300, 50, "", 20, onMouseClickRunnable);
    }

    public Button(int width, int height, Runnable onMouseClickRunnable) {
        this(null, 0, 0, width, height, "", 20, onMouseClickRunnable);
    }

    @Override
    public void onMouseHover() {
        if (collideSound != null) {
            Core.get().getSoundManager().play(new SoundSource(collideSound));
        }
    }

    @Override
    public void update() {
        super.update();
        stringObject.update();
    }

    @Override
    public boolean onMouseLeftClick() {
        if (!isMouseHover()) return false;

        onMouseClickRunnable.run();

        if (clickSound != null) {
            Core.get().getSoundManager().play(new SoundSource(clickSound));
        }

        return true;
    }

    @Override
    public boolean onMouseRightClick() {
        if (!isMouseHover()) return false;
        onMouseRightClickRunnable.run();
        return true;
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
        stringObject.setPosition(x + stringXOffset, y + stringCache.getCenteredYOffset(stringObject.getString(), height, stringObject.getFontSize()) + stringYOffset);
        stringObject.update();
        return this;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        stringObject.setX(x + stringXOffset);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        StringCache stringCache = stringObject.getStringCache();
        stringObject.setY(y + stringCache.getCenteredYOffset(stringObject.getString(), height, stringObject.getFontSize()) + stringYOffset);
    }

    public void setStringXOffset(int stringXOffset) {
        this.stringXOffset = stringXOffset;
        stringObject.setX(x + stringXOffset);
    }

    @Override
    public Button setTextColor(float r, float g, float b, float a) {
        stringObject.setColor(r, g, b, a).compile();
        return this;
    }

    public Button setTextColor(Vector4f color) {
        return setTextColor(color.x, color.y, color.z, color.w);
    }

    public void setString(String string) {
        stringObject.setString(string);
    }

    public String getString() {
        return stringObject.getString();
    }
}