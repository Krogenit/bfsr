package net.bfsr.editor.gui.renderer;

import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.engine.gui.renderer.MinimizableGuiObjectRenderer;

public class InspectionEntryRenderer extends MinimizableGuiObjectRenderer {
    private final InspectionEntry<?> inspectionEntry;

    public InspectionEntryRenderer(InspectionEntry<?> guiObject) {
        super(guiObject);
        this.inspectionEntry = guiObject;
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);

        if (inspectionEntry.isSelected()) {
            guiRenderer.setColor(id, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
        } else if (guiObject.isMouseHover()) {
            guiRenderer.setColor(id, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }

    @Override
    protected void setLastUpdateValues() {
        super.setLastUpdateValues();
        if (inspectionEntry.isSelected()) {
            guiRenderer.setLastColor(id, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
        } else if (guiObject.isMouseHover()) {
            guiRenderer.setLastColor(id, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }

    @Override
    protected void renderBase() {
        if (inspectionEntry.isSelected()) {
            guiRenderer.addDrawCommand(id);
        } else if (guiObject.isMouseHover()) {
            guiRenderer.addDrawCommand(id);
        }
    }

    @Override
    public void updatePosition() {
        super.updatePosition();
        int x = guiObject.getSceneX();
        int y = guiObject.getSceneY();
        if (inspectionEntry.isSelected()) {
            guiRenderer.setPosition(id, x, y + inspectionEntry.getHeight() - inspectionEntry.getBaseHeight());
        } else if (guiObject.isMouseHover()) {
            guiRenderer.setPosition(id, x, y);
        }
    }

    @Override
    public void updateSize() {
        super.updateSize();
        if (inspectionEntry.isSelected()) {
            guiRenderer.setSize(id, guiObject.getWidth(), inspectionEntry.getBaseHeight());
        } else if (guiObject.isMouseHover()) {
            guiRenderer.setSize(id, guiObject.getWidth(), guiObject.getHeight());
        }
    }
}
