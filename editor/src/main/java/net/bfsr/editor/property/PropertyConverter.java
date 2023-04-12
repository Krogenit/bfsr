package net.bfsr.editor.property;

public interface PropertyConverter<V> {
    String toString(V value);
    V fromString(String value);
}