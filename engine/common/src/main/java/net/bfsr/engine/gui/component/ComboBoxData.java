package net.bfsr.engine.gui.component;

import lombok.Getter;
import net.bfsr.engine.gui.renderer.RectangleOutlinedRenderer;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.StringOffsetType;

@Getter
public class ComboBoxData<V> extends GuiObject {
    private final V value;
    private final Label label;
    private final int stringOffsetY;
    private final int fontSize;

    ComboBoxData(int width, int height, V value, String name, Font font, int fontSize, int stringOffsetY) {
        super(width, height);
        this.stringOffsetY = stringOffsetY;
        this.fontSize = fontSize;
        this.value = value;

        add(this.label = new Label(font, name, fontSize, StringOffsetType.CENTERED));
        label.atBottomLeft(width / 2, label.getCenteredOffsetY(height));
        setRenderer(new RectangleOutlinedRenderer(this));
    }

    @Override
    public ComboBoxData<V> setTextColor(float r, float g, float b, float a) {
        label.setColor(r, g, b, a);
        return this;
    }

    @Override
    public GuiObject setWidth(int width) {
        label.atBottomLeft(width / 2, label.getCenteredOffsetY(height));
        return super.setWidth(width);
    }
}