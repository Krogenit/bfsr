package net.bfsr.editor.gui.inspection;

import lombok.Getter;
import net.bfsr.client.gui.GuiObject;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.property.PropertiesHolder;

import java.util.ArrayList;
import java.util.List;

public class InspectionEntry<T extends PropertiesHolder> extends InspectionMinimizableGuiObject<T> {
    @Getter
    protected final List<T> objects = new ArrayList<>();

    public InspectionEntry(InspectionPanel<T> inspectionPanel, int width, int height, String name, FontType fontType, int fontSize, int stringYOffset) {
        super(inspectionPanel, width, height, name, fontType, fontSize, stringYOffset);
        setOnMaximizeRunnable(inspectionPanel::updatePositions);
        setOnMinimizeRunnable(inspectionPanel::updatePositions);
    }

    public void addObject(T object) {
        for (int i = 0; i < objects.size(); i++) {
            T t = objects.get(i);
            if (t.getClass() == object.getClass()) {
                return;
            }
        }

        objects.add(object);
    }

    public T getComponentByType(Class<T> type) {
        for (int i = 0; i < objects.size(); i++) {
            T t = objects.get(i);
            if (t.getClass() == type) {
                return t;
            }
        }

        return null;
    }

    @Override
    protected void onSelected() {
        inspectionPanel.setWantSelectObject(this);
    }

    @Override
    protected void onStartMoving() {
        inspectionPanel.setMovableObject(this);
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
    protected void onNameChanged(String name) {
        super.onNameChanged(name);
    }

    @Override
    public void minimize() {
        if (inspectionPanel.getMovableObject() == null) {
            super.minimize();
        }
    }
}