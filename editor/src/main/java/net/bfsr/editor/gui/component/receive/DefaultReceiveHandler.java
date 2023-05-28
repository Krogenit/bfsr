package net.bfsr.editor.gui.component.receive;

import net.bfsr.editor.gui.component.ComponentHolder;
import net.bfsr.editor.property.PropertiesHolder;

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