package net.bfsr.editor.gui.builder;

import net.bfsr.editor.gui.property.PropertyComponent;
import net.bfsr.editor.gui.property.PropertyMap;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.renderer.font.Font;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MapBuilder extends ComponentBuilder {
    @Override
    public PropertyComponent build(int width, int height, String propertyName, int offsetX, Font font, int fontSize,
                                   int stringOffsetY, List<Field> fields, Object[] values, Object object,
                                   BiConsumer<Object, Integer> valueSetterConsumer) throws IllegalAccessException {
        Object value = values[0];
        Map<Object, Object> map = (Map<Object, Object>) value;
        Field field = fields.get(0);
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Type[] actualTypeArguments = type.getActualTypeArguments();

        PropertyMap<Object> propertyMap = new PropertyMap<>(width, height, propertyName, font, fontSize, offsetX,
                stringOffsetY,
                object,
                fields, values, valueSetterConsumer, () -> {
            Class<Object> keyClass = (Class<Object>) actualTypeArguments[0];
            if (keyClass.isEnum()) {
                return keyClass.getEnumConstants()[0];
            } else {
                try {
                    return keyClass.getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }, () -> {
            try {
                return (PropertiesHolder) ((Class<Object>) actualTypeArguments[1]).getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

        map.forEach((o, o2) -> propertyMap.add(o, (PropertiesHolder) o2));

        return propertyMap;
    }
}