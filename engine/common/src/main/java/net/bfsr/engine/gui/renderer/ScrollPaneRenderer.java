package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.gui.component.scroll.Scroll;
import net.bfsr.engine.renderer.opengl.GL;

import java.util.List;

public class ScrollPaneRenderer extends GuiObjectRenderer {
    private final Scroll scroll;

    public ScrollPaneRenderer(ScrollPane guiObject, Scroll scroll) {
        super(guiObject);
        this.scroll = scroll;
    }

    @Override
    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        guiRenderer.render();
        Engine.renderer.glEnable(GL.GL_SCISSOR_TEST);
        Engine.renderer.glScissor(x, Engine.renderer.getScreenHeight() - (y + height), width, height);

        List<GuiObject> guiObjects = guiObject.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject = guiObjects.get(i);
            if (this.guiObject.isIntersects(guiObject)) {
                guiObject.render(guiRenderer, lastX, lastY, x, y);
            }
        }

        guiRenderer.render();
        Engine.renderer.glDisable(GL.GL_SCISSOR_TEST);

        scroll.render(guiRenderer, lastX, lastY, x, y);
    }
}
