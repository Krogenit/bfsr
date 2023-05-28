package net.bfsr.editor.property.converter;

public class StringConverter implements PropertyConverter<String> {
    @Override
    public String toString(String value) {
        return value;
    }

    @Override
    public String fromString(String value) {
        return value;
    }
}