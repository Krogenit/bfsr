package net.bfsr.editor.property;

import net.bfsr.render.RenderLayer;
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
        CONVERTER_MAP.put(RenderLayer.class, new RenderLayerConverter());
    }

    public static PropertyConverter<?> getConverter(Class<?> clazz) {
        return CONVERTER_MAP.get(clazz);
    }
}