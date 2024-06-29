package net.bfsr.engine.gui;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.AbstractRenderer;

public abstract class Gui extends GuiObject {
    protected final AbstractRenderer renderer = Engine.renderer;
    protected Gui parentGui;

    protected Gui() {
        this(null);
    }

    protected Gui(Gui parentGui) {
        super(Engine.renderer.getScreenWidth(), Engine.renderer.getScreenHeight());
        this.parentGui = parentGui;
    }

    @Override
    public void onScreenResize(int width, int height) {
        this.width = width;
        this.height = height;
        for (int i = 0, size = guiObjects.size(); i < size; i++) {
            guiObjects.get(i).onScreenResize(width, height);
        }
    }
}