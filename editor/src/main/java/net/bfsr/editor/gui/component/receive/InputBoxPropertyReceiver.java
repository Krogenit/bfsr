package net.bfsr.editor.gui.component.receive;

import net.bfsr.editor.gui.component.ComponentHolder;
import net.bfsr.engine.gui.component.InputBox;

public class InputBoxPropertyReceiver extends InputBox implements DragTarget {
    private final PropertyReceiver propertyReceiver;

    protected InputBoxPropertyReceiver(int width, int height, String string, String fontName, int fontSize, int stringOffsetX,
                                       int stringOffsetY, int maxLineSize, PropertyReceiver propertyReceiver) {
        super(width, height, string, fontName, fontSize, stringOffsetX, stringOffsetY, maxLineSize);
        this.propertyReceiver = propertyReceiver;
    }

    @Override
    public boolean canAcceptDraggable(ComponentHolder componentHolder) {
        return propertyReceiver.canInsert(componentHolder);
    }

    @Override
    public void acceptDraggable(ComponentHolder componentHolder) {
        setString(propertyReceiver.getValueForInputBox(componentHolder));
    }
}