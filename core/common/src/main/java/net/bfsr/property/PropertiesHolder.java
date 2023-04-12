package net.bfsr.property;

import net.bfsr.property.event.ChangeNameEventListener;

public interface PropertiesHolder {
    void setDefaultValues();
    void setName(String name);
    String getName();
    void registerChangeNameEventListener(ChangeNameEventListener listener);
    void clearListeners();
}