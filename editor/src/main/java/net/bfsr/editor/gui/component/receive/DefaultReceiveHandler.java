package net.bfsr.editor.gui.component.receive;

import net.bfsr.editor.gui.component.ComponentHolder;

public class DefaultReceiveHandler implements PropertyReceiver {
    @Override
    public boolean canInsert(ComponentHolder componentHolder) {
        return false;
    }

    @Override
    public String getValueForInputBox(ComponentHolder componentHolder) {
        throw new UnsupportedOperationException("");
    }
}