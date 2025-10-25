package net.bfsr.engine.gui.component;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.renderer.RectangleRenderer;
import net.bfsr.engine.gui.renderer.RectangleTexturedRenderer;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.font.string.StringOffsetType;
import net.bfsr.engine.renderer.texture.TextureRegister;
import org.joml.Vector4f;

import java.util.function.BiConsumer;

import static net.bfsr.engine.renderer.font.AbstractFontManager.DEFAULT_FONT_NAME;

public class Button extends GuiObject {
    private final Label label;

    public Button(int width, int height, String string, Font font, int fontSize, int stringXOffset, int stringYOffset,
                  StringOffsetType stringOffsetType, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        super(width, height);
        label = new Label(font, string, stringXOffset, 0, fontSize, stringOffsetType);
        add(label.atBottomLeft(stringXOffset, stringYOffset));
        setLeftReleaseConsumer(leftReleaseConsumer);
        setHoverColor(0.5f, 1.0f, 1.0f, 1.0f);
        setRenderer(new RectangleRenderer(this));
    }

    public Button(int width, int height, String string, Font font, int fontSize, int stringYOffset,
                  BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(width, height, string, font, fontSize, width / 2, stringYOffset,
                StringOffsetType.CENTERED, leftReleaseConsumer);
    }

    public Button(int width, int height, String string, Font font, int fontSize, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(width, height, string, font, fontSize, font.getCenteredOffsetY(string, height, fontSize), leftReleaseConsumer);
    }

    public Button(int width, int height, String string, int fontSize, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(width, height, string, Engine.getFontManager().getFont(DEFAULT_FONT_NAME), fontSize, leftReleaseConsumer);
    }

    public Button(TextureRegister texture, int width, int height, String string, int fontSize) {
        this(texture, width, height, string, fontSize, EMPTY_BI_CONSUMER);
    }

    public Button(TextureRegister texture, int width, int height, String string, int fontSize,
                  BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(width, height, string, fontSize, leftReleaseConsumer);
        setRenderer(new RectangleTexturedRenderer(this, texture));
    }

    public Button(TextureRegister texture, int width, int height, String string, Font font, int fontSize,
                  BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(width, height, string, font, fontSize, leftReleaseConsumer);
        setRenderer(new RectangleTexturedRenderer(this, texture));
    }

    public Button(int width, int height, String string, Font font, int fontSize, int stringYOffset) {
        this(width, height, string, font, fontSize, stringYOffset, EMPTY_BI_CONSUMER);
    }

    public Button(int width, int height, String string, Font font, int fontSize) {
        this(width, height, string, font, fontSize, font.getCenteredOffsetY(string, height, fontSize), EMPTY_BI_CONSUMER);
    }

    public Button(String string, int fontSize, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(TextureRegister.guiButtonBase, 300, 50, string, fontSize, leftReleaseConsumer);
    }

    public Button(int width, int height, String string, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(TextureRegister.guiButtonBase, width, height, string, 20, leftReleaseConsumer);
    }

    public Button(String string, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(TextureRegister.guiButtonBase, 300, 50, string, 20, leftReleaseConsumer);
    }

    public Button(TextureRegister texture, int width, int height, String string) {
        this(texture, width, height, string, 20, EMPTY_BI_CONSUMER);
    }

    public Button(TextureRegister texture, int width, int height, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(texture, width, height, "", 20, leftReleaseConsumer);
    }

    public Button(TextureRegister texture, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(texture, 300, 50, "", 20, leftReleaseConsumer);
    }

    public Button(int width, int height, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        this(width, height, "", 20, leftReleaseConsumer);
    }

    public Button(int width, int height) {
        this(width, height, "", 20, EMPTY_BI_CONSUMER);
    }

    public Button setStringXOffset(int stringXOffset) {
        label.atBottomLeft(stringXOffset, label.getCenteredOffsetY(height));
        updatePositionAndSize();
        return this;
    }

    @Override
    public Button setTextColor(float r, float g, float b, float a) {
        label.setColor(r, g, b, a);
        return this;
    }

    public Button setTextColor(Vector4f color) {
        return setTextColor(color.x, color.y, color.z, color.w);
    }

    public void setString(String string) {
        label.setString(string);
    }

    public String getString() {
        return label.getString();
    }
}