package net.bfsr.editor.gui.inspection;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.gui.GuiObject;
import net.bfsr.client.gui.GuiObjectWithSubObjects;
import net.bfsr.client.gui.GuiObjectsHandler;
import net.bfsr.client.gui.input.InputBox;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import net.bfsr.editor.gui.ColorScheme;
import net.bfsr.editor.gui.component.MinimizableGuiObject;
import net.bfsr.property.ComponentHolder;
import net.bfsr.property.PropertiesHolder;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.ColorScheme.setupColors;

public class InspectionEntry<T extends PropertiesHolder> extends MinimizableGuiObject implements ComponentHolder<T> {
    protected final InspectionPanel<T> inspectionPanel;

    @Getter
    protected final List<T> components = new ArrayList<>();
    @Setter
    @Getter
    protected GuiObjectWithSubObjects parent;

    private boolean clicked;
    private boolean selected, wasSelected;
    protected final Vector2i selectPosition = new Vector2i();
    private InputBox inputBox;

    public InspectionEntry(InspectionPanel<T> inspectionPanel, int width, int height, String name, FontType fontType, int fontSize, int stringYOffset) {
        super(width, height, name, fontType, fontSize, stringYOffset);
        this.inspectionPanel = inspectionPanel;
        setupColors(this);
        setCanMaximize(false);
        setOnMaximizeRunnable(inspectionPanel::updatePositions);
        setOnMinimizeRunnable(inspectionPanel::updatePositions);
    }

    @Override
    public void onUnregistered(GuiObjectsHandler gui) {
        super.onUnregistered(gui);
        selected = false;
        if (inputBox != null) {
            gui.unregisterGuiObject(inputBox);
        }
    }

    @Override
    public void onOtherGuiObjectMouseLeftClick(GuiObject guiObject) {
        if (inspectionPanel.isIntersectsWithMouse() && selected && guiObject != inputBox) {
            unselect();
        }
    }

    @Override
    public void onOtherGuiObjectMouseRightClick(GuiObject guiObject) {
        if (inspectionPanel.isIntersectsWithMouse() && selected && guiObject != inputBox) {
            unselect();
        }
    }

    @Override
    protected void onStartMoving() {
        inspectionPanel.setMovableObject(this);
    }

    @Override
    protected void onMoved() {
        inspectionPanel.onEntryMoved(this);
    }

    @Override
    public void update() {
        super.update();

        if (clicked && Mouse.isLeftDown() && selectPosition.lengthSquared() > 0) {
            Vector2f mousePosition = Mouse.getPosition();
            float moveThreshold = 40;
            if (mousePosition.distanceSquared(selectPosition.x, selectPosition.y) > moveThreshold) {
                onStartMoving();
            }
        }
    }

    protected void onSelected() {
        inspectionPanel.setWantSelectObject(this);
    }

    private void select() {
        selected = true;
    }

    private void unselect() {
        selected = false;
    }

    @Override
    public void minimize() {
        if (inspectionPanel.getMovableObject() == null) {
            super.minimize();
        }
    }

    @Override
    public boolean onMouseLeftClick() {
        if (!inspectionPanel.isIntersectsWithMouse()) return false;

        wasSelected = selected;

        if (!isMouseHover() && (inputBox == null || !inputBox.isMouseHover())) {
            if (selected) {
                unselect();
            }

            return false;
        }

        Vector2f mousePosition = Mouse.getPosition();
        int mouseX = (int) mousePosition.x;
        selectPosition.set(mouseX, (int) mousePosition.y);
        clicked = true;

        if (!selected && inputBox == null) {
            int selectOffsetX = canMaximize ? MINIMIZABLE_STRING_X_OFFSET : 0;
            if (mouseX >= x + selectOffsetX && mouseX < x + width) {
                select();
            }
        }

        return super.onMouseLeftClick();
    }

    @Override
    public void onMouseLeftRelease() {
        clicked = false;

        if (!isMouseHover()) return;

        Vector2f mousePosition = Mouse.getPosition();
        int mouseX = (int) mousePosition.x;
        int selectOffsetX = canMaximize ? MINIMIZABLE_STRING_X_OFFSET : 0;
        if (canMaximize && mouseX >= x && mouseX < x + MINIMIZABLE_STRING_X_OFFSET) {
            if (maximized) {
                minimize();
            } else {
                maximize();
            }
        } else if (selected) {
            if (mouseX >= x + selectOffsetX && mouseX < x + width) {
                if (wasSelected) {
                    inputBox = new InputBox(width - selectOffsetX, height, "", stringObject.getStringCache(), fontSize, 3, stringYOffset, 300);
                    inputBox.setOnUnselectedRunnable(() -> {
                        gui.unregisterGuiObject(inputBox);
                        onNameChanged(inputBox.getString());
                        inputBox = null;
                    });
                    ColorScheme.setupInputBoxColors(inputBox);
                    inputBox.setString(getName());
                    subObjectsRepositionConsumer.setup(inputBox, selectOffsetX, 0);
                    gui.registerGuiObject(inputBox);
                    inputBox.enableTyping();
                    selected = false;
                } else {
                    onSelected();
                }
            }
        }
    }

    @Override
    public boolean onMouseRightClick() {
        wasSelected = selected;

        if (!isMouseHover() && (inputBox == null || !inputBox.isMouseHover())) {
            if (selected) {
                unselect();
            }

            return false;
        }

        Vector2f mousePosition = Mouse.getPosition();
        int mouseX = (int) mousePosition.x;

        if (!selected && inputBox == null) {
            int selectOffsetX = canMaximize ? MINIMIZABLE_STRING_X_OFFSET : 0;
            if (mouseX >= x + selectOffsetX && mouseX < x + width) {
                selected = true;
            }
        }

        return super.onMouseRightClick();
    }

    @Override
    protected void renderBase() {
        if (selected) {
            GUIRenderer.get().add(lastX, lastY, x, y, width, height, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
        } else if (isMouseHover()) {
            GUIRenderer.get().add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }

    public void addComponent(T component) {
        for (int i = 0; i < components.size(); i++) {
            T t = components.get(i);
            if (t.getClass() == component.getClass()) {
                return;
            }
        }

        components.add(component);
    }

    @Override
    public void addSubObject(AbstractGuiObject guiObject) {
        super.addSubObject(guiObject);
        setCanMaximize(true);
        if (guiObject instanceof InspectionEntry<?> inspectionMinimizableGuiObject) {
            inspectionMinimizableGuiObject.setParent(this);
        }
    }

    @Override
    public void removeSubObject(AbstractGuiObject guiObject) {
        super.removeSubObject(guiObject);
        if (subObjects.size() == 0) {
            setCanMaximize(false);
        }
    }

    @Override
    public T getComponentByType(Class<T> type) {
        for (int i = 0; i < components.size(); i++) {
            T t = components.get(i);
            if (t.getClass() == type) {
                return t;
            }
        }

        return null;
    }

    @Override
    protected void onNameChanged(String name) {
        super.onNameChanged(name);
        for (int i = 0; i < components.size(); i++) {
            components.get(i).setName(name);
        }
    }
}