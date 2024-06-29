package net.bfsr.engine.gui.component;

import net.bfsr.engine.gui.renderer.RectangleRenderer;
import net.bfsr.engine.gui.renderer.RectangleTexturedRenderer;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector4f;

public class Button extends GuiObject {
    private final Label label;

    public Button(int x, int y, int width, int height, String string, FontType fontType, int fontSize, int stringXOffset, int stringYOffset,
                  StringOffsetType stringOffsetType, Runnable leftReleaseRunnable) {
        super(x, y, width, height);
        this.leftReleaseRunnable = leftReleaseRunnable;
        add(this.label = new Label(fontType, string, stringXOffset, fontType.getStringCache()
                .getCenteredYOffset(string, height, fontSize) + stringYOffset, fontSize, stringOffsetType).compileAtOrigin());
        setHoverColor(0.5f, 1.0f, 1.0f, 1.0f);
        setRenderer(new RectangleRenderer(this));
    }

    public Button(int x, int y, int width, int height, String string, FontType fontType, int fontSize, int stringYOffset,
                  Runnable leftReleaseRunnable) {
        this(x, y, width, height, string, fontType, fontSize, width / 2, stringYOffset, StringOffsetType.CENTERED, leftReleaseRunnable);
    }

    public Button(int x, int y, int width, int height, String string, FontType fontType, int fontSize, Runnable leftReleaseRunnable) {
        this(x, y, width, height, string, fontType, fontSize, 0, leftReleaseRunnable);
    }

    public Button(int x, int y, int width, int height, String string, int fontSize, Runnable leftReleaseRunnable) {
        this(x, y, width, height, string, FontType.XOLONIUM, fontSize, leftReleaseRunnable);
    }

    public Button(TextureRegister texture, int x, int y, int width, int height, String string, int fontSize) {
        this(texture, x, y, width, height, string, fontSize, RunnableUtils.EMPTY_RUNNABLE);
    }

    public Button(TextureRegister texture, int x, int y, int width, int height, String string, int fontSize, Runnable leftReleaseRunnable) {
        this(x, y, width, height, string, fontSize, leftReleaseRunnable);
        setRenderer(new RectangleTexturedRenderer(this, texture));
    }

    public Button(TextureRegister texture, int width, int height, String string, int fontSize, Runnable leftReleaseRunnable) {
        this(texture, 0, 0, width, height, string, fontSize, leftReleaseRunnable);
    }

    public Button(int width, int height, String string, FontType fontType, int fontSize, int stringYOffset, Runnable leftReleaseRunnable) {
        this(0, 0, width, height, string, fontType, fontSize, stringYOffset, leftReleaseRunnable);
    }

    public Button(int width, int height, String string, FontType fontType, int fontSize, int stringYOffset) {
        this(0, 0, width, height, string, fontType, fontSize, stringYOffset, RunnableUtils.EMPTY_RUNNABLE);
    }

    public Button(int width, int height, String string, int fontSize, Runnable leftReleaseRunnable) {
        this(width, height, string, FontType.XOLONIUM, fontSize, 0, leftReleaseRunnable);
    }

    public Button(String string, int fontSize, Runnable leftReleaseRunnable) {
        this(TextureRegister.guiButtonBase, 300, 50, string, fontSize, leftReleaseRunnable);
    }

    public Button(int width, int height, String string, Runnable leftReleaseRunnable) {
        this(TextureRegister.guiButtonBase, 0, 0, width, height, string, 20, leftReleaseRunnable);
    }

    public Button(String string, Runnable leftReleaseRunnable) {
        this(TextureRegister.guiButtonBase, 0, 0, 300, 50, string, 20, leftReleaseRunnable);
    }

    public Button(TextureRegister texture, int width, int height, Runnable leftReleaseRunnable) {
        this(texture, 0, 0, width, height, "", 20, leftReleaseRunnable);
    }

    public Button(TextureRegister texture, Runnable leftReleaseRunnable) {
        this(texture, 0, 0, 300, 50, "", 20, leftReleaseRunnable);
    }

    public Button(int width, int height, Runnable leftReleaseRunnable) {
        this(0, 0, width, height, "", 20, leftReleaseRunnable);
    }

    public Button setStringXOffset(int stringXOffset) {
        label.setX(stringXOffset);
        return this;
    }

    @Override
    public Button setTextColor(float r, float g, float b, float a) {
        label.setColor(r, g, b, a).compileAtOrigin();
        return this;
    }

    public Button setTextColor(Vector4f color) {
        return setTextColor(color.x, color.y, color.z, color.w);
    }

    public void setString(String string) {
        label.setStringAndCompileAtOrigin(string);
    }

    public String getString() {
        return label.getString();
    }
}