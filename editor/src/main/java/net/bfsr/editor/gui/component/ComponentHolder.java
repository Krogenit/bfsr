package net.bfsr.editor.gui.component;

import net.bfsr.editor.property.PropertiesHolder;

public interface ComponentHolder<T extends PropertiesHolder> {
    T getComponentByType(Class<T> type);
}