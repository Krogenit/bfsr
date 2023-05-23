package net.bfsr.property;

import java.util.List;

public interface ComponentHolder<T extends PropertiesHolder> {
    List<T> getComponents();
    T getComponentByType(Class<T> type);
}