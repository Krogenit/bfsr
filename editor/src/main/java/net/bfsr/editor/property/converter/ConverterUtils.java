package net.bfsr.editor.property.converter;

import net.bfsr.engine.config.Vector2fConfigurable;
import net.bfsr.engine.config.Vector2iConfigurable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

public final class ConverterUtils {
    private static final Map<Class<?>, PropertyConverter<?>> CONVERTER_MAP = new HashMap<>();

    static {
        CONVERTER_MAP.put(String.class, new StringConverter());
        CONVERTER_MAP.put(boolean.class, new BooleanConverter());
        CONVERTER_MAP.put(float.class, new FloatConverter());
        CONVERTER_MAP.put(int.class, new IntegerConverter());
        CONVERTER_MAP.put(Vector2f.class, new Vector2fConverter());
        CONVERTER_MAP.put(Vector4f.class, new Vector4fConverter());
        CONVERTER_MAP.put(Enum.class, new EnumConverter());
        CONVERTER_MAP.put(Vector2fConfigurable.class, new Vector2fConfigurableConverter());
        CONVERTER_MAP.put(Vector2iConfigurable.class, new Vector2iConfigurableConverter());
    }

    public static PropertyConverter<?> getConverter(Class<?> clazz) {
        for (Map.Entry<Class<?>, PropertyConverter<?>> entry : CONVERTER_MAP.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) {
                return entry.getValue();
            }
        }

        return null;
    }
}