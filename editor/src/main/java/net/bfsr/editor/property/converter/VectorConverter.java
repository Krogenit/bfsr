package net.bfsr.editor.property.converter;

import net.bfsr.engine.util.DecimalUtils;

import java.util.function.Function;

public class VectorConverter<T> implements PropertyConverter<T> {
    private static final String SEPARATOR = ",";

    private final Function<float[], T> convertFromFunction;
    private final Function<T, float[]> convertToFunction;

    public VectorConverter(Function<float[], T> convertFromFunction, Function<T, float[]> convertToFunction) {
        this.convertFromFunction = convertFromFunction;
        this.convertToFunction = convertToFunction;
    }

    @Override
    public String toString(T value) {
        return toString(convertToFunction.apply(value));
    }

    @Override
    public T fromString(Class<T> classType, String value) {
        String[] strings = value.split(SEPARATOR);
        float[] values = new float[strings.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = Float.parseFloat(strings[i]);
        }

        return convertFromFunction.apply(values);
    }

    private String toString(float... values) {
        StringBuilder string = new StringBuilder(DecimalUtils.formatWithTwoDigits(values[0]));

        for (int i = 1; i < values.length; i++) {
            string.append(SEPARATOR).append(DecimalUtils.formatWithTwoDigits(values[i]));
        }

        return string.toString();
    }
}