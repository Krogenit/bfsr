package net.bfsr.editor.gui.component;

import net.bfsr.editor.property.holder.PropertiesHolder;

public interface ComponentHolder {
    <COMPONENT_TYPE extends PropertiesHolder> COMPONENT_TYPE getComponentByType(Class<COMPONENT_TYPE> type);
}