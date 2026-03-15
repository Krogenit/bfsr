package net.bfsr.engine.gui.renderer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Setter;
import net.bfsr.engine.gui.component.GuiObject;

public class RectangleOutlinedRenderer extends RectangleRenderer {
    private final IntList outlineIds = new IntArrayList(4);
    @Setter
    private int outlineSize = 1;

    public RectangleOutlinedRenderer(GuiObject guiObject) {
        super(guiObject);
    }

    @Override
    protected void create() {
        super.create();

        outlineIds.add(guiRenderer.add(guiObject.getSceneX(), guiObject.getSceneY(), outlineSize, guiObject.getHeight(), outlineColor));
        outlineIds.add(guiRenderer.add(guiObject.getSceneX() + outlineSize, guiObject.getSceneY(), guiObject.getWidth() - outlineSize,
                outlineSize, outlineColor));
        outlineIds.add(guiRenderer.add(guiObject.getSceneX() + guiObject.getWidth() - outlineSize,
                guiObject.getSceneY() + outlineSize, outlineSize, guiObject.getHeight() - (outlineSize << 1), outlineColor));
        outlineIds.add(guiRenderer.add(guiObject.getSceneX() + outlineSize, guiObject.getSceneY() + guiObject.getHeight() - outlineSize,
                guiObject.getWidth() - outlineSize, outlineSize, outlineColor));

        for (int i = 0; i < outlineIds.size(); i++) {
            idList.add(outlineIds.getInt(i));
        }
    }

    @Override
    protected void setLastUpdateValues() {
        super.setLastUpdateValues();

        int sceneX = guiObject.getSceneX();
        int sceneY = guiObject.getSceneY();
        guiRenderer.setLastPosition(outlineIds.getInt(0), sceneX, sceneY);
        guiRenderer.setLastPosition(outlineIds.getInt(1), sceneX + outlineSize, sceneY);
        guiRenderer.setLastPosition(outlineIds.getInt(2), sceneX + guiObject.getWidth() - outlineSize, sceneY + outlineSize);
        guiRenderer.setLastPosition(outlineIds.getInt(3), sceneX + outlineSize, sceneY + guiObject.getHeight() - outlineSize);

        guiRenderer.setLastSize(outlineIds.getInt(0), outlineSize, guiObject.getHeight());
        guiRenderer.setLastSize(outlineIds.getInt(1), guiObject.getWidth() - outlineSize, outlineSize);
        guiRenderer.setLastSize(outlineIds.getInt(2), outlineSize, guiObject.getHeight() - (outlineSize << 1));
        guiRenderer.setLastSize(outlineIds.getInt(3), guiObject.getWidth() - outlineSize, outlineSize);

        guiRenderer.setLastColor(outlineIds.getInt(0), activeOutlineColor);
        guiRenderer.setLastColor(outlineIds.getInt(1), activeOutlineColor);
        guiRenderer.setLastColor(outlineIds.getInt(2), activeOutlineColor);
        guiRenderer.setLastColor(outlineIds.getInt(3), activeOutlineColor);
    }

    @Override
    protected void renderBody() {
        super.renderBody();
        for (int i = 0; i < outlineIds.size(); i++) {
            guiRenderer.addDrawCommand(outlineIds.getInt(i));
        }
    }

    @Override
    public void onMouseHover() {
        super.onMouseHover();
        for (int i = 0; i < outlineIds.size(); i++) {
            guiRenderer.setColor(outlineIds.getInt(i), activeOutlineColor);
        }
    }

    @Override
    public void onMouseStopHover() {
        super.onMouseStopHover();
        for (int i = 0; i < outlineIds.size(); i++) {
            guiRenderer.setColor(outlineIds.getInt(i), activeOutlineColor);
        }
    }

    @Override
    public void updatePosition() {
        super.updatePosition();
        int sceneX = guiObject.getSceneX();
        int sceneY = guiObject.getSceneY();
        guiRenderer.setPosition(outlineIds.getInt(0), sceneX, sceneY);
        guiRenderer.setPosition(outlineIds.getInt(1), sceneX + outlineSize, sceneY);
        guiRenderer.setPosition(outlineIds.getInt(2), sceneX + guiObject.getWidth() - outlineSize, sceneY + outlineSize);
        guiRenderer.setPosition(outlineIds.getInt(3), sceneX + outlineSize, sceneY + guiObject.getHeight() - outlineSize);
    }

    @Override
    public void updateSize() {
        super.updateSize();
        guiRenderer.setSize(outlineIds.getInt(0), outlineSize, guiObject.getHeight());
        guiRenderer.setSize(outlineIds.getInt(1), guiObject.getWidth() - outlineSize, outlineSize);
        guiRenderer.setSize(outlineIds.getInt(2), outlineSize, guiObject.getHeight() - (outlineSize << 1));
        guiRenderer.setSize(outlineIds.getInt(3), guiObject.getWidth() - outlineSize, outlineSize);
    }

    @Override
    protected void removeRenderIds() {
        super.removeRenderIds();
        outlineIds.clear();
    }
}