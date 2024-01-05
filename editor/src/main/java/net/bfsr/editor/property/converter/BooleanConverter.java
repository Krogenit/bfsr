package net.bfsr.editor.property.converter;

public class BooleanConverter implements PropertyConverter<Boolean> {
    @Override
    public String toString(Boolean value) {
        return value.toString();
    }

    @Override
    public Boolean fromString(Class<Boolean> classType, String value) {
        return Boolean.parseBoolean(value);
    }
}