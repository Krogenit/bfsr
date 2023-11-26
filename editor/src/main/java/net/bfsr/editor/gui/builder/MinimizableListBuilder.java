package net.bfsr.editor.gui.builder;

import net.bfsr.editor.gui.property.MinimizablePropertyList;
import net.bfsr.editor.gui.property.PropertyComponent;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.BiConsumer;

public class MinimizableListBuilder extends ComponentBuilder {
    @Override
    public PropertyComponent build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize,
                                   int stringOffsetY, List<Field> fields, Object[] values, Object object,
                                   BiConsumer<Object, Integer> valueSetterConsumer) {
        Object value = values[0];
        List<PropertiesHolder> objects = (List<PropertiesHolder>) value;
        Field field = fields.get(0);
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class<PropertiesHolder> listElementClass = (Class<PropertiesHolder>) type.getActualTypeArguments()[0];
        Property annotation = field.getAnnotation(Property.class);

        MinimizablePropertyList minimizablePropertyList = new MinimizablePropertyList(width, height, propertyName, fontType,
                fontSize, offsetX, stringOffsetY, () -> {
            try {
                return listElementClass.getConstructor().newInstance();
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }, object, fields, values, valueSetterConsumer, annotation.arrayElementName());

        for (int i = 0; i < objects.size(); i++) {
            minimizablePropertyList.add(objects.get(i));
        }

        return minimizablePropertyList;
    }
}