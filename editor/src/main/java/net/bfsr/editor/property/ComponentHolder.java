package net.bfsr.editor.property;

public interface ComponentHolder<T extends PropertiesHolder> {
    T getComponentByType(Class<T> type);
}