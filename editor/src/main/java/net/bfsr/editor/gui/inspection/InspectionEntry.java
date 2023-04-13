package net.bfsr.editor.gui.inspection;

import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import net.bfsr.property.PropertiesHolder;

public class InspectionEntry<T extends PropertiesHolder> extends InspectionMinimizableGuiObject<T> {
    public InspectionEntry(InspectionPanel<T> inspectionPanel, int width, int height, FontType fontType, int fontSize, int stringYOffset) {
        super(inspectionPanel, width, height, "Entry", fontType, fontSize, stringYOffset);
        setOnMaximizeRunnable(inspectionPanel::updatePositions);
        setOnMinimizeRunnable(inspectionPanel::updatePositions);
    }

    @Override
    protected void onStartMoving() {
        inspectionPanel.setMovableObject(this);
    }

    @Override
    public void minimize() {
        if (inspectionPanel.getMovableObject() == null) {
            super.minimize();
        }
    }

    @Override
    protected void renderBase() {
        if (isMouseHover()) {
            GUIRenderer.get().add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }
}