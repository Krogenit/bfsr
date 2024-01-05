package net.bfsr.editor.property.converter;

public class EnumConverter implements PropertyConverter<Enum> {
    @Override
    public String toString(Enum value) {
        return value.toString();
    }

    @Override
    public Enum<?> fromString(Class<Enum> classType, String value) {
        return Enum.valueOf(classType, value);
    }
}