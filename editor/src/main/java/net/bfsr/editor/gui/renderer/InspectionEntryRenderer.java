package net.bfsr.editor.gui.renderer;

import net.bfsr.editor.gui.inspection.InspectionEntry;

public class InspectionEntryRenderer extends MinimizableGuiObjectRenderer {
    private final InspectionEntry<?> inspectionEntry;

    public InspectionEntryRenderer(InspectionEntry<?> guiObject) {
        super(guiObject);
        this.inspectionEntry = guiObject;
    }

    @Override
    protected void renderBase(int lastX, int lastY, int x, int y, int width, int height) {
        if (inspectionEntry.isSelected()) {
            guiRenderer.add(lastX, lastY, x, y, width, inspectionEntry.getBaseHeight(), 35 / 255.0f, 74 / 255.0f, 108 / 255.0f,
                    hoverColor.w);
        } else if (guiObject.isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }
}
