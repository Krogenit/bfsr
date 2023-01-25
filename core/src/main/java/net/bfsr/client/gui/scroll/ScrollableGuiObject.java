package net.bfsr.client.gui.scroll;

import lombok.Getter;

@Getter
final class ScrollableGuiObject {
    private int y;
    private final Scrollable scrollable;

    ScrollableGuiObject(Scrollable scrollable) {
        this.scrollable = scrollable;
        this.y = scrollable.getY();
    }

    public void updateY() {
        this.y = scrollable.getY();
    }
}
