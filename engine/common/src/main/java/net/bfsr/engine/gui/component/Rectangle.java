package net.bfsr.engine.gui.component;

import net.bfsr.engine.gui.renderer.RectangleRenderer;

public class Rectangle extends GuiObject {
    public Rectangle() {
        setRenderer(new RectangleRenderer(this));
    }

    public Rectangle(int x, int y, int width, int height) {
        super(x, y, width, height);
        setRenderer(new RectangleRenderer(this));
    }

    public Rectangle(int width, int height) {
        super(width, height);
        setRenderer(new RectangleRenderer(this));
    }
}
