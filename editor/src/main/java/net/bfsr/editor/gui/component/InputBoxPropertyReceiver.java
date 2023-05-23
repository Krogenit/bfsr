package net.bfsr.editor.gui.component;

import net.bfsr.client.gui.input.InputBox;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.property.ComponentHolder;
import net.bfsr.property.PropertiesHolder;

public class InputBoxPropertyReceiver extends InputBox implements DragTarget {
    public InputBoxPropertyReceiver(int width, int height, String string, FontType fontType, int fontSize, int stringOffsetX, int stringOffsetY, int maxLineSize) {
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