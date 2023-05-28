package net.bfsr.editor.gui.component.receive;

import net.bfsr.editor.gui.component.ComponentHolder;
import net.bfsr.editor.property.PropertiesHolder;

public interface DragTarget {
    boolean canAcceptDraggable(ComponentHolder<? extends PropertiesHolder> componentHolder);
    void acceptDraggable(ComponentHolder<? extends PropertiesHolder> componentHolder);
}