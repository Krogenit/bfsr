package net.bfsr.editor.gui.builder;

import net.bfsr.editor.gui.property.PropertyComponent;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class ComponentBuilder {
    private static final EnumMap<PropertyGuiElementType, ComponentBuilder> BUILDERS = new EnumMap<>(PropertyGuiElementType.class);

    static {
        BUILDERS.put(PropertyGuiElementType.INPUT_BOX, new InputBoxBuilder());
        BUILDERS.put(PropertyGuiElementType.FILE_SELECTOR, new FileSelectorBuilder());
        BUILDERS.put(PropertyGuiElementType.CHECK_BOX, new CheckBoxBuilder());
        BUILDERS.put(PropertyGuiElementType.COMBO_BOX, new ComboBoxBuilder());
        BUILDERS.put(PropertyGuiElementType.SIMPLE_LIST, new ListBuilder());
        BUILDERS.put(PropertyGuiElementType.MINIMIZABLE_LIST, new MinimizableListBuilder());
        BUILDERS.put(PropertyGuiElementType.POLYGON, new PolygonBuilder());
        BUILDERS.put(PropertyGuiElementType.MAP, new MapBuilder());
        BUILDERS.put(PropertyGuiElementType.OBJECT, new ObjectBuilder());
    }

    public abstract PropertyComponent build(int width, int height, String propertyName, int offsetX, FontType fontType,
                                            int fontSize, int stringOffsetY, List<Field> fields, Object[] values, Object object,
                                            BiConsumer<Object, Integer> valueSetterConsumer)
            throws IllegalAccessException;

    public static PropertyComponent build(PropertyGuiElementType type, int width, int height, String propertyName, int offsetX,
                                          FontType fontType, int fontSize, int stringOffsetY, List<Field> fields, Object[] values,
                                          Object object, BiConsumer<Object, Integer> valueSetterConsumer)
            throws IllegalAccessException {
        return BUILDERS.get(type).build(width, height, propertyName, offsetX, fontType, fontSize, stringOffsetY, fields, values,
                object, valueSetterConsumer);
    }
}