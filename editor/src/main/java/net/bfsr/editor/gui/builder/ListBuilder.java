package net.bfsr.editor.gui.builder;

import net.bfsr.editor.gui.property.PropertyComponent;
import net.bfsr.editor.gui.property.SimplePropertyList;
import net.bfsr.editor.property.Property;
import net.bfsr.engine.renderer.font.glyph.Font;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class ListBuilder extends ComponentBuilder {
    @Override
    public PropertyComponent build(int width, int height, String propertyName, int offsetX, Font font, int fontSize,
                                   int stringOffsetY, List<Field> fields, Object[] values, Object object,
                                   BiConsumer<Object, Integer> valueSetterConsumer, Runnable changeValueListener) {
        Object value = values[0];
        List<?> objects = (List<?>) value;
        Field field = fields.get(0);
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class<?> listElementClass = (Class<?>) type.getActualTypeArguments()[0];
        Property annotation = field.getAnnotation(Property.class);

        SimplePropertyList property = createProperty(width, height, propertyName, offsetX, font, fontSize, stringOffsetY, fields,
                values, object, valueSetterConsumer, annotation, () -> {
                    try {
                        return listElementClass.getConstructor().newInstance();
                    } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }, changeValueListener);

        if (objects != null) {
            for (int i = 0; i < objects.size(); i++) {
                property.addProperty(objects.get(i));
            }
        }

        return property;
    }

    protected SimplePropertyList createProperty(int width, int height, String propertyName, int offsetX, Font font, int fontSize,
                                                int stringOffsetY, List<Field> fields, Object[] values, Object object,
                                                BiConsumer<Object, Integer> valueSetterConsumer, Property annotation, Supplier supplier,
                                                Runnable changeValueListener) {
        return new SimplePropertyList<>(width, height, propertyName, font, fontSize, offsetX, stringOffsetY, supplier, object, fields,
                values, annotation.arrayElementType(), annotation.arrayElementName(), valueSetterConsumer, changeValueListener);
    }
}