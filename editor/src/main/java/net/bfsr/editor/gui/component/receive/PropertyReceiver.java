package net.bfsr.editor.gui.component.receive;

import net.bfsr.editor.gui.component.ComponentHolder;

public interface PropertyReceiver {
    boolean canInsert(ComponentHolder componentHolder);
    String getValueForInputBox(ComponentHolder componentHolder);
}