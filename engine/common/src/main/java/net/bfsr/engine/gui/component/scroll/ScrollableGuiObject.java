package net.bfsr.engine.gui.component.scroll;

import lombok.Getter;
import net.bfsr.engine.gui.component.GuiObject;

@Getter
final class ScrollableGuiObject {
    private int y;
    private final GuiObject guiObject;

    ScrollableGuiObject(GuiObject guiObject) {
        this.guiObject = guiObject;
        this.y = guiObject.getY();
    }

    void updateY() {
        this.y = guiObject.getY();
    }

    @Override
    public boolean equals(Object obj) {
        return ((ScrollableGuiObject) obj).guiObject.equals(guiObject);
    }
}