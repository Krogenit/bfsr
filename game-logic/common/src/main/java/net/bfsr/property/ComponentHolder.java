package net.bfsr.property;

public interface ComponentHolder<T extends PropertiesHolder> {
    T getComponentByType(Class<T> type);
}