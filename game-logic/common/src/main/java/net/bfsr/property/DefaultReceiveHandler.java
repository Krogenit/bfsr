package net.bfsr.property;

import net.bfsr.property.event.PropertyReceiver;

public class DefaultReceiveHandler<T extends PropertiesHolder> implements PropertyReceiver<T> {
    @Override
    public boolean canInsert(ComponentHolder<T> componentHolder) {
        return false;
    }

    @Override
    public String getValueForInputBox(ComponentHolder<T> componentHolder) {
        throw new UnsupportedOperationException();
    }
}