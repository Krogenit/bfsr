package net.bfsr.client.gui.scroll;

import lombok.Getter;
import net.bfsr.client.gui.GuiObject;

@Getter
final class ScrollableGuiObject {
    private int y;
    private final GuiObject guiObject;

    ScrollableGuiObject(GuiObject guiObject) {
        this.guiObject = guiObject;
        this.y = guiObject.getYForScroll();
    }

    public void updateY() {
        this.y = guiObject.getYForScroll();
    }

    @Override
    public boolean equals(Object obj) {
        return ((ScrollableGuiObject) obj).getGuiObject().equals(guiObject);
    }
}