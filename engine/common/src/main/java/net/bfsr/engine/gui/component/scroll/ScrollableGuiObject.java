package net.bfsr.engine.gui.component.scroll;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.gui.component.GuiObject;

@Getter
final class ScrollableGuiObject {
    @Setter
    private int y;
    private final GuiObject guiObject;

    ScrollableGuiObject(GuiObject guiObject) {
        this.guiObject = guiObject;
        this.y = guiObject.getY();
    }

    @Override
    public boolean equals(Object obj) {
        return ((ScrollableGuiObject) obj).guiObject.equals(guiObject);
    }
}