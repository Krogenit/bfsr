package net.bfsr.editor.gui.component.receive;

import net.bfsr.editor.gui.component.ComponentHolder;

public interface DragTarget {
    boolean canAcceptDraggable(ComponentHolder componentHolder);
    void acceptDraggable(ComponentHolder componentHolder);
}