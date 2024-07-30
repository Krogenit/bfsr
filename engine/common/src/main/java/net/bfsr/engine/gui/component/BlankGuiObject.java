package net.bfsr.engine.gui.component;

import net.bfsr.engine.Engine;

public final class BlankGuiObject extends GuiObject {
    public static final BlankGuiObject INSTANCE = new BlankGuiObject();

    private BlankGuiObject() {
        setWidthFunction((width, height) -> Engine.renderer.getScreenWidth());
        setHeightFunction((width, height) -> Engine.renderer.getScreenHeight());
    }

    @Override
    protected void onChildSizeChanged(GuiObject guiObject, int width, int height) {}

    @Override
    protected void onChildPositionChanged(GuiObject guiObject, int x, int y) {}

    @Override
    public int getSceneX() {
        return 0;
    }

    @Override
    public int getSceneY() {
        return 0;
    }
}
