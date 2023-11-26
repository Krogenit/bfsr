package net.bfsr.editor.property.holder;

import net.bfsr.editor.property.event.ChangeNameEventListener;

public class PropertiesHolderAdapter implements PropertiesHolder {
    @Override
    public void setDefaultValues() {}

    @Override
    public void setName(String name) {}

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void registerChangeNameEventListener(ChangeNameEventListener listener) {}

    @Override
    public void clearListeners() {}
}