package net.bfsr.editor.gui.property;

import lombok.Getter;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.MinimizableGuiObject;
import net.bfsr.engine.renderer.font.glyph.Font;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class PropertyComponent extends MinimizableGuiObject {
    @Getter
    protected final Object object;
    protected final Object[] values;
    protected final List<Field> fields;
    protected int propertyOffsetX;
    final int propertyOffsetY;
    protected final BiConsumer<Object, Integer> valueConsumer;
    protected final Runnable changeValueListener;
    int baseWidth;

    protected PropertyComponent(int width, int height, String name, Font font, int fontSize, int propertyOffsetX, int propertyOffsetY,
                                int minimizableStringOffsetX, int stringOffsetY, Object object, List<Field> fields, Object[] values,
                                BiConsumer<Object, Integer> valueConsumer, Runnable changeValueListener) {
        super(width, height, name, font, fontSize, 0, stringOffsetY, minimizableStringOffsetX, minimizableStringOffsetX);
        this.baseWidth = width;
        this.object = object;
        this.fields = fields;
        this.values = values;
        this.propertyOffsetX = propertyOffsetX;
        this.propertyOffsetY = propertyOffsetY;
        this.valueConsumer = valueConsumer;
        this.changeValueListener = changeValueListener;
        setTextColor(EditorTheme.TEXT_COLOR_GRAY, EditorTheme.TEXT_COLOR_GRAY, EditorTheme.TEXT_COLOR_GRAY, 1.0f);
        setHoverColor(0.3f, 0.3f, 0.3f, 0.5f);
        setCanMaximize(false);
    }

    protected PropertyComponent(int width, int height, String name, Font font, int fontSize, int propertyOffsetX, int propertyOffsetY,
                                int stringOffsetY, Object object, List<Field> fields, Object[] values,
                                BiConsumer<Object, Integer> valueConsumer, Runnable changeValueListener) {
        this(width, height, name, font, fontSize, propertyOffsetX, propertyOffsetY, 0, stringOffsetY, object, fields, values,
                valueConsumer, changeValueListener);
    }

    public abstract void setSetting() throws IllegalAccessException;

    @Override
    public GuiObject setWidth(int width) {
        this.baseWidth = width;
        return super.setWidth(width);
    }

    public void setPropertyOffsetX(int propertyOffsetX) {
        this.propertyOffsetX = propertyOffsetX;
        updateConcealableObjectsPositions();
    }
}