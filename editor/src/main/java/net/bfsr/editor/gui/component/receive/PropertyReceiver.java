package net.bfsr.editor.gui.component.receive;

import net.bfsr.editor.gui.component.ComponentHolder;
import net.bfsr.editor.property.PropertiesHolder;

public interface PropertyReceiver<T extends PropertiesHolder> {
    boolean canInsert(ComponentHolder<T> componentHolder);
    String getValueForInputBox(ComponentHolder<T> componentHolder);
}