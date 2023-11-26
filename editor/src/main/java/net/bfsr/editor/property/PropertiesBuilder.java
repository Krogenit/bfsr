package net.bfsr.editor.property;

import lombok.extern.log4j.Log4j2;
import net.bfsr.editor.gui.builder.ComponentBuilder;
import net.bfsr.editor.gui.property.PropertyComponent;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Log4j2
public final class PropertiesBuilder {
    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        return fields;
    }

    public static void createGuiProperties(Object object, int width, int height, FontType fontType, int fontSize,
                                           int propertyOffsetX, int stringYOffset,
                                           Consumer<PropertyComponent> consumer) {
        createGuiProperties(object, width, height, fontType, fontSize, propertyOffsetX, stringYOffset, consumer, "");
    }

    public static void createGuiProperties(Object object, int width, int height, FontType fontType, int fontSize,
                                           int propertyOffsetX, int stringYOffset,
                                           Consumer<PropertyComponent> consumer, String propertyName) {
        List<Field> fields = getAllFields(new LinkedList<>(), object.getClass());
        int fieldsAmount = 0;
        List<Field> fieldsBulk = new ArrayList<>(4);
        PropertyGuiElementType elementType = null;
        boolean customPropertyName = !propertyName.isEmpty();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Property annotation = field.getDeclaredAnnotation(Property.class);
            if (annotation != null && field.getDeclaredAnnotation(Deprecated.class) == null) {
                if (elementType == null) {
                    fieldsAmount = annotation.fieldsAmount();
                    propertyName = customPropertyName ? propertyName : annotation.name().isEmpty() ? field.getName() :
                            annotation.name();
                    elementType = annotation.elementType();
                }

                Class<?> fieldType = field.getType();
                if (PropertiesHolder.class.isAssignableFrom(fieldType) && elementType != PropertyGuiElementType.OBJECT) {
                    field.setAccessible(true);

                    try {
                        createGuiProperties(field.get(object), width, height, fontType, fontSize, propertyOffsetX, stringYOffset,
                                consumer, propertyName);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    elementType = null;
                    continue;
                } else {
                    fieldsBulk.add(field);
                }

                if (fieldsBulk.size() == fieldsAmount) {
                    try {
                        consumer.accept(createProperty(object, width, height, propertyOffsetX, fontType, fontSize, stringYOffset,
                                propertyName,
                                elementType, new ArrayList<>(fieldsBulk)));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    fieldsBulk.clear();
                    elementType = null;
                }
            }
        }
    }

    private static PropertyComponent createProperty(Object object, int width, int height, int propertyOffsetX,
                                                    FontType fontType, int fontSize, int stringYOffset,
                                                    String propertyName, PropertyGuiElementType elementType,
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

        return ComponentBuilder.build(elementType, width, height, propertyName, propertyOffsetX, fontType, fontSize,
                stringYOffset,
                fields, values, object, (o, integer) -> {
                    try {
                        fields.get(integer).set(object, o);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}