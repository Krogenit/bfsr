package net.bfsr.engine.gui.renderer;

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
    public void render(int mouseX, int mouseY) {
        guiRenderer.render();
        renderer.glEnable(GL.GL_SCISSOR_TEST);
        renderer.glScissor(guiObject.getSceneX(), guiObject.getSceneY(), guiObject.getWidth(), guiObject.getHeight());

        List<GuiObject> guiObjects = guiObject.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject = guiObjects.get(i);
            if (this.guiObject.isIntersects(guiObject)) {
                guiObject.getRenderer().render(mouseX, mouseY);
            }
        }

        guiRenderer.render();
        renderer.glDisable(GL.GL_SCISSOR_TEST);

        scroll.getRenderer().render(mouseX, mouseY);
    }
}
