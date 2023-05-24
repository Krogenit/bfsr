package net.bfsr.editor.property;

import lombok.extern.log4j.Log4j2;
import net.bfsr.editor.gui.builder.ComponentBuilder;
import net.bfsr.editor.gui.component.PropertyComponent;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Log4j2
public final class PropertiesBuilder {
    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        return fields;
    }

    public static <V extends PropertiesHolder> void createGuiProperties(V object, int width, int height, FontType fontType, int fontSize, int propertyOffsetX, int stringYOffset,
                                                                        Consumer<PropertyComponent<V>> consumer) {
        List<Field> fields = getAllFields(new LinkedList<>(), object.getClass());
        int fieldsAmount = 0;
        List<Field> fieldsBulk = new ArrayList<>(4);
        String propertyName = "";
        PropertyGuiElementType elementType = null;

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Property annotation = field.getDeclaredAnnotation(Property.class);
            if (annotation != null && field.getDeclaredAnnotation(Deprecated.class) == null) {
                fieldsBulk.add(field);

                if (elementType == null) {
                    fieldsAmount = annotation.fieldsAmount();
                    propertyName = annotation.name().isEmpty() ? field.getName() : annotation.name();
                    elementType = annotation.elementType();
                }

                if (fieldsBulk.size() == fieldsAmount) {
                    try {
                        consumer.accept(createProperty(object, width, height, propertyOffsetX, fontType, fontSize, stringYOffset, propertyName, elementType, fieldsBulk));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    fieldsBulk.clear();
                    elementType = null;
                }
            }
        }
    }

    private static <V extends PropertiesHolder> PropertyComponent<V> createProperty(V object, int width, int height, int propertyOffsetX, FontType fontType, int fontSize,
                                                                                    int stringYOffset, String propertyName, PropertyGuiElementType elementType,
                                                                                    List<Field> fields) throws IllegalAccessException {
        Object[] values = new Object[fields.size()];

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                values[i] = value;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return ComponentBuilder.build(elementType, width, height, propertyName, propertyOffsetX, fontType, fontSize, stringYOffset, new ArrayList<>(fields), values, object);
    }
}