package net.bfsr.editor.gui.builder;

import net.bfsr.editor.gui.component.PropertyComponent;
import net.bfsr.editor.property.PropertiesHolder;
import net.bfsr.editor.property.PropertyGuiElementType;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

public abstract class ComponentBuilder {
    private static final EnumMap<PropertyGuiElementType, ComponentBuilder> BUILDERS = new EnumMap<>(PropertyGuiElementType.class);

    static {
        BUILDERS.put(PropertyGuiElementType.INPUT_BOX, new InputBoxBuilder());
        BUILDERS.put(PropertyGuiElementType.MAIN_NAME_INPUT_BOX, new MainNameInputBoxBuilder());
        BUILDERS.put(PropertyGuiElementType.FILE_SELECTOR, new FileSelectorBuilder());
        BUILDERS.put(PropertyGuiElementType.CHECK_BOX, new CheckBoxBuilder());
        BUILDERS.put(PropertyGuiElementType.COMBO_BOX, new ComboBoxBuilder());
        BUILDERS.put(PropertyGuiElementType.ARRAY, new ArrayBuilder());
    }

    public abstract <P extends PropertiesHolder> PropertyComponent<P> build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize,
                                                                            int stringOffsetY, List<Field> fields, Object[] values,
                                                                            P object) throws IllegalAccessException;

    public <P extends PropertiesHolder, PRIMITIVE_TYPE> PropertyComponent<P> build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize,
                                                                                   int stringOffsetY, List<Field> fields, Object[] values,
                                                                                   Consumer<PRIMITIVE_TYPE> valueConsumer, Class<?> fieldType) {
        throw new UnsupportedOperationException();
    }

    public static <P extends PropertiesHolder> PropertyComponent<P> build(PropertyGuiElementType type, int width, int height, String propertyName, int offsetX,
                                                                          FontType fontType, int fontSize, int stringOffsetY, List<Field> fields, Object[] values,
                                                                          P object) throws IllegalAccessException {
        return BUILDERS.get(type).build(width, height, propertyName, offsetX, fontType, fontSize, stringOffsetY, fields, values, object);
    }

    public static <P extends PropertiesHolder, PRIMITIVE_TYPE> PropertyComponent<P> build(PropertyGuiElementType type, int width, int height, String propertyName, int offsetX,
                                                                                          FontType fontType, int fontSize, int stringOffsetY, List<Field> fields, Object[] values,
                                                                                          Consumer<PRIMITIVE_TYPE> valueConsumer, Class<?> fieldType) throws IllegalAccessException {
        return BUILDERS.get(type).build(width, height, propertyName, offsetX, fontType, fontSize, stringOffsetY, fields, values, valueConsumer, fieldType);
    }
}