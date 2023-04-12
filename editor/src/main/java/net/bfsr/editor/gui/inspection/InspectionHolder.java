package net.bfsr.editor.gui.inspection;

import lombok.Getter;
import net.bfsr.client.gui.GuiObject;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.property.PropertiesHolder;

public class InspectionHolder<T extends PropertiesHolder> extends InspectionMinimizableGuiObject<T> {
    @Getter
    protected final T object;

    public InspectionHolder(InspectionPanel<T> inspectionPanel, int width, int height, FontType fontType, int fontSize, int stringYOffset, T object) {
        super(inspectionPanel, width, height, object.getName(), fontType, fontSize, stringYOffset);
        this.object = object;
        setCanMaximize(false);
    }

    @Override
    protected void onSelected() {
        inspectionPanel.setWantSelectObject(this);
    }

    @Override
    protected void onUnselected() {
        inspectionPanel.setWantUnselect(true);
    }

    @Override
    public void onOtherGuiObjectMouseLeftClick(GuiObject guiObject) {
        if (!inspectionPanel.isMouseHover()) return;

        super.onOtherGuiObjectMouseLeftClick(guiObject);
    }

    @Override
    public void onOtherGuiObjectMouseRightClick(GuiObject guiObject) {
        if (!inspectionPanel.isMouseHover()) return;

        super.onOtherGuiObjectMouseRightClick(guiObject);
    }

    @Override
    public boolean onMouseLeftClick() {
        if (!inspectionPanel.isMouseHover()) return false;

        return super.onMouseLeftClick();
    }

    @Override
    protected void onStartMoving() {
        inspectionPanel.setMovableObject(this);
    }

    @Override
    protected void onNameChanged(String name) {
        super.onNameChanged(name);
        object.setName(name);
    }
}