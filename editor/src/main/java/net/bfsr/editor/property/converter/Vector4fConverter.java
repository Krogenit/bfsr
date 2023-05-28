package net.bfsr.editor.property.converter;

import net.bfsr.util.DecimalUtils;
import org.joml.Vector4f;

public class Vector4fConverter implements PropertyConverter<Vector4f> {
    private final String separator = ",";

    @Override
    public String toString(Vector4f value) {
        return DecimalUtils.formatWithToDigits(value.x) + separator + DecimalUtils.formatWithToDigits(value.y) +
                separator + DecimalUtils.formatWithToDigits(value.z) + separator + DecimalUtils.formatWithToDigits(value.w);
    }

    @Override
    public Vector4f fromString(String value) {
        String[] strings = value.split(separator);
        return new Vector4f(Float.parseFloat(strings[0]), Float.parseFloat(strings[1]), Float.parseFloat(strings[2]), Float.parseFloat(strings[3]));
    }
}