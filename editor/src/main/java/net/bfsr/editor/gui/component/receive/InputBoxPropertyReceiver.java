package net.bfsr.editor.gui.component.receive;

import net.bfsr.editor.gui.component.ComponentHolder;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.renderer.font.FontType;

public class InputBoxPropertyReceiver extends InputBox implements DragTarget {
    private final PropertyReceiver propertyReceiver;

    protected InputBoxPropertyReceiver(int width, int height, String string, FontType fontType, int fontSize, int stringOffsetX,
                                       int stringOffsetY, int maxLineSize, PropertyReceiver propertyReceiver) {
        super(width, height, string, fontType, fontSize, stringOffsetX, stringOffsetY, maxLineSize);
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