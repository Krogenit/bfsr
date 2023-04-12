package net.bfsr.editor.property;

import net.bfsr.util.DecimalUtils;
import org.joml.Vector2f;

public class Vector2fConverter implements PropertyConverter<Vector2f> {
    private final String separator = ",";

    @Override
    public String toString(Vector2f value) {
        return DecimalUtils.formatWithToDigits(value.x) + separator + DecimalUtils.formatWithToDigits(value.y);
    }

    @Override
    public Vector2f fromString(String value) {
        String[] strings = value.split(separator);
        return new Vector2f(Float.parseFloat(strings[0]), Float.parseFloat(strings[1]));
    }
}