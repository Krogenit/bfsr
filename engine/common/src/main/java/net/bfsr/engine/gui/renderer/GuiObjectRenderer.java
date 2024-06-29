package net.bfsr.engine.gui.renderer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import org.joml.Vector4f;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class GuiObjectRenderer {
    protected AbstractRenderer renderer = Engine.renderer;
    protected AbstractGUIRenderer guiRenderer = renderer.guiRenderer;

    protected final GuiObject guiObject;

    protected final Vector4f color;
    protected final Vector4f outlineColor;
    protected final Vector4f hoverColor;
    protected final Vector4f outlineHoverColor;

    public GuiObjectRenderer(GuiObject guiObject) {
        this.guiObject = guiObject;
        this.color = guiObject.getColor();
        this.outlineColor = guiObject.getOutlineColor();
        this.hoverColor = guiObject.getHoverColor();
        this.outlineHoverColor = guiObject.getOutlineHoverColor();
    }

    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        renderChild(lastX, lastY, x, y);
    }

    protected void renderChild(int lastX, int lastY, int x, int y) {
        List<GuiObject> guiObjects = guiObject.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).render(guiRenderer, lastX, lastY, x, y);
        }
    }
}
