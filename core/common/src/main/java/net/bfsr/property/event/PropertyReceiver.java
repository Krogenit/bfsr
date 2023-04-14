package net.bfsr.property.event;

import net.bfsr.property.ComponentHolder;
import net.bfsr.property.PropertiesHolder;

public interface PropertyReceiver<T extends PropertiesHolder> {
    boolean canInsert(ComponentHolder<T> componentHolder);
    String getValueForInputBox(ComponentHolder<T> componentHolder);
}