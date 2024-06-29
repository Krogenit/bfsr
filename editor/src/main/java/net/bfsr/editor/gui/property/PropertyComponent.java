package net.bfsr.editor.gui.property;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.component.MinimizableGuiObject;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class PropertyComponent extends MinimizableGuiObject {
    @Getter
    protected final Object object;
    protected final Object[] values;
    protected final List<Field> fields;
    @Setter
    protected int propertyOffsetX;
    final int propertyOffsetY;
    protected final BiConsumer<Object, Integer> valueConsumer;
    int baseWidth;

    protected PropertyComponent(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX,
                                int propertyOffsetY, int minimizableStringOffsetX, int stringOffsetY, Object object, List<Field> fields,
                                Object[] values, BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, fontType, fontSize, 0, stringOffsetY, minimizableStringOffsetX);
        this.baseWidth = width;
        this.object = object;
        this.fields = fields;
        this.values = values;
        this.propertyOffsetX = propertyOffsetX;
        this.propertyOffsetY = propertyOffsetY;
        this.valueConsumer = valueConsumer;
        setTextColor(EditorTheme.TEXT_COLOR_GRAY, EditorTheme.TEXT_COLOR_GRAY, EditorTheme.TEXT_COLOR_GRAY, 1.0f);
        setHoverColor(0.3f, 0.3f, 0.3f, 0.5f);
        setCanMaximize(false);
    }

    protected PropertyComponent(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX,
                                int propertyOffsetY, int stringOffsetY, Object object, List<Field> fields, Object[] values,
                                BiConsumer<Object, Integer> valueConsumer) {
        this(width, height, name, fontType, fontSize, propertyOffsetX, propertyOffsetY, 0, stringOffsetY, object, fields, values,
                valueConsumer);
    }

    @Override
    public GuiObject setWidth(int width) {
        this.baseWidth = width;
        return super.setWidth(width);
    }

    public abstract void setSetting() throws IllegalAccessException;
}