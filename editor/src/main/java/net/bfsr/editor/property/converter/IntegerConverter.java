package net.bfsr.editor.property.converter;

public class IntegerConverter implements PropertyConverter<Integer> {
    @Override
    public String toString(Integer value) {
        return value.toString();
    }

    @Override
    public Integer fromString(Class<Integer> classType, String value) {
        return Integer.parseInt(value);
    }
}