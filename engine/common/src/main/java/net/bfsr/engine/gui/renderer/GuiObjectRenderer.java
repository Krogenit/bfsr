package net.bfsr.engine.gui.renderer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector4f;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class GuiObjectRenderer {
    protected AbstractRenderer renderer = Engine.renderer;
    protected AbstractGUIRenderer guiRenderer = renderer.guiRenderer;

    protected final GuiObject guiObject;

    protected IntArrayList idList = new IntArrayList();

    protected int lastX, lastY;
    protected float lastRotation;

    protected final Vector4f color;
    protected final Vector4f outlineColor;
    protected final Vector4f hoverColor;
    protected final Vector4f outlineHoverColor;

    private Runnable lastValuesUpdater = RunnableUtils.EMPTY_RUNNABLE;

    public GuiObjectRenderer(GuiObject guiObject) {
        this.guiObject = guiObject;
        this.color = guiObject.getColor();
        this.outlineColor = guiObject.getOutlineColor();
        this.hoverColor = guiObject.getHoverColor();
        this.outlineHoverColor = guiObject.getOutlineHoverColor();
    }

    public void addToScene() {
        create();
        lastValuesUpdater = this::setLastUpdateValues;
    }

    protected void create() {}

    public void update() {
        updateLastValues();
    }

    public void updateLastValues() {
        lastX = guiObject.getSceneX();
        lastY = guiObject.getSceneY();
        lastRotation = guiObject.getRotation();
        lastValuesUpdater.run();
    }

    protected void setLastUpdateValues() {}

    public void render() {
        renderChild();
    }

    protected void renderChild() {
        List<GuiObject> guiObjects = guiObject.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject = guiObjects.get(i);
            guiObject.getRenderer().render();
        }
    }

    public void onMouseHover() {}

    public void onMouseStopHover() {}

    public void updatePosition() {}

    public void updatePosition(int x, int y) {}

    public void updateRotation() {}

    public void updateSize() {}

    public void updateColor() {}

    public void remove() {
        removeRenderIds();
        lastValuesUpdater = RunnableUtils.EMPTY_RUNNABLE;
    }

    protected void removeRenderIds() {
        while (idList.size() > 0) {
            guiRenderer.removeObject(idList.removeInt(0));
        }
    }
}
