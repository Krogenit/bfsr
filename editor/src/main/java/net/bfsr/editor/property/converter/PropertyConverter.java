package net.bfsr.editor.property.converter;

public interface PropertyConverter<V> {
    String toString(V value);
    V fromString(String value);
}