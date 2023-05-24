package net.bfsr.editor.property.event;

import net.bfsr.editor.property.ComponentHolder;
import net.bfsr.editor.property.PropertiesHolder;

public interface PropertyReceiver<T extends PropertiesHolder> {
    boolean canInsert(ComponentHolder<T> componentHolder);
    String getValueForInputBox(ComponentHolder<T> componentHolder);
}