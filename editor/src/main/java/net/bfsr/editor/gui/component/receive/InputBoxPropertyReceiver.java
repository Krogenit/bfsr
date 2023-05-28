package net.bfsr.editor.gui.component.receive;

import net.bfsr.editor.gui.component.ComponentHolder;
import net.bfsr.editor.property.PropertiesHolder;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.renderer.font.FontType;

public class InputBoxPropertyReceiver extends InputBox implements DragTarget {
    public InputBoxPropertyReceiver(int width, int height, String string, FontType fontType, int fontSize, int stringOffsetX,
                                    int stringOffsetY, int maxLineSize) {
        super(width, height, string, fontType, fontSize, stringOffsetX, stringOffsetY, maxLineSize);
    }

    @Override
    public boolean canAcceptDraggable(ComponentHolder<? extends PropertiesHolder> componentHolder) {
        return false;
    }

    @Override
    public void acceptDraggable(ComponentHolder<? extends PropertiesHolder> componentHolder) {

    }
}