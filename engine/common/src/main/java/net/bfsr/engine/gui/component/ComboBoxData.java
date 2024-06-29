package net.bfsr.engine.gui.component;

import lombok.Getter;
import net.bfsr.engine.gui.renderer.RectangleOutlinedRenderer;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;

@Getter
public class ComboBoxData<V> extends GuiObject {
    private final V value;
    private final Label label;
    private final int stringOffsetY;
    private final int fontSize;

    ComboBoxData(int width, int height, V value, String name, FontType fontType, int fontSize, int stringOffsetY) {
        super(width, height);
        this.stringOffsetY = stringOffsetY;
        this.fontSize = fontSize;
        this.value = value;

        add(this.label = new Label(fontType, name, fontSize, StringOffsetType.CENTERED).compileAtOrigin());
        label.atTopLeft(width / 2,
                label.getStringCache().getCenteredYOffset(label.getString(), height, fontSize) + stringOffsetY);
        setRenderer(new RectangleOutlinedRenderer(this));
    }

    @Override
    public ComboBoxData<V> setTextColor(float r, float g, float b, float a) {
        label.setColor(r, g, b, a).compileAtOrigin();
        return this;
    }

    @Override
    public GuiObject setWidth(int width) {
        label.atTopLeft(width / 2,
                label.getStringCache().getCenteredYOffset(label.getString(), height, fontSize) + stringOffsetY);
        return super.setWidth(width);
    }
}