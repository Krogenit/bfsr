package net.bfsr.editor.property;

public class IntegerConverter implements PropertyConverter<Integer> {
    @Override
    public String toString(Integer value) {
        return value.toString();
    }

    @Override
    public Integer fromString(String value) {
        return Integer.parseInt(value);
    }
}