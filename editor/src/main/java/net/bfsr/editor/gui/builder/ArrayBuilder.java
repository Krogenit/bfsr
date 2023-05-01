package net.bfsr.editor.gui.builder;

import net.bfsr.client.renderer.font.FontType;
import net.bfsr.editor.gui.component.PropertyComponent;
import net.bfsr.editor.gui.component.PropertyObjectArray;
import net.bfsr.editor.gui.component.PropertyPrimitiveArray;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.property.Property;
import net.bfsr.property.PropertyGuiElementType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class ArrayBuilder extends ComponentBuilder {
    @Override
    public <V extends PropertiesHolder> PropertyComponent<V> build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize, int stringOffsetY,
                                                                   List<Field> fields, Object[] values, V object) {
        Object value = values[0];
        List<?> objects = (List<?>) value;
        Field field = fields.get(0);
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class<?> listElementClass = (Class<?>) type.getActualTypeArguments()[0];

        if (PropertiesHolder.class.isAssignableFrom(listElementClass)) {
            PropertyObjectArray<V> propertyObjectArray = new PropertyObjectArray<>(width, height, propertyName, fontType, fontSize, offsetX, stringOffsetY, () -> {
                try {
                    return (V) listElementClass.getConstructor().newInstance();
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }, object, fields, values);

            for (int i = 0; i < objects.size(); i++) {
                propertyObjectArray.add((V) objects.get(i));
            }

            return propertyObjectArray;
        } else {
            Property annotation = field.getAnnotation(Property.class);
            PropertyGuiElementType propertyGuiElementType = annotation.arrayElementType();
            String arrayElementPropertyName = annotation.arrayElementName();

            PropertyPrimitiveArray propertyPrimitiveArray = new PropertyPrimitiveArray<>(width, height, propertyName, fontType, fontSize, offsetX, stringOffsetY, () -> {
                try {
                    return listElementClass.getConstructor().newInstance();
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }, object, fields, values, propertyGuiElementType, arrayElementPropertyName);

            if (objects != null) {
                for (int i = 0; i < objects.size(); i++) {
                    propertyPrimitiveArray.add(objects.get(i));
                }
            }

            return propertyPrimitiveArray;
        }
    }
}