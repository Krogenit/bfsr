package net.bfsr.editor.property;

public class BooleanConverter implements PropertyConverter<Boolean> {
    @Override
    public String toString(Boolean value) {
        return value.toString();
    }

    @Override
    public Boolean fromString(String value) {
        return Boolean.parseBoolean(value);
    }
}