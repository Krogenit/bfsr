package net.bfsr.editor.property.holder;

import net.bfsr.editor.property.event.ChangeNameEventListener;

public class PropertiesHolderAdapter implements PropertiesHolder {
    @Override
    public void setDefaultValues() {}

    @Override
    public void addChangeNameEventListener(ChangeNameEventListener listener) {}

    @Override
    public void clearListeners() {}
}