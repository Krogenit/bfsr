package net.bfsr.editor.gui.inspection;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.component.ComponentHolder;
import net.bfsr.editor.gui.component.MinimizableGuiObject;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.object.AbstractGuiObject;
import net.bfsr.engine.gui.object.GuiObject;
import net.bfsr.engine.gui.object.GuiObjectWithSubObjects;
import net.bfsr.engine.gui.object.GuiObjectsHandler;
import net.bfsr.engine.renderer.font.FontType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.setupColors;

public class InspectionEntry<T extends PropertiesHolder> extends MinimizableGuiObject implements ComponentHolder {
    private final InspectionPanel<T> inspectionPanel;

    private final List<T> components = new ArrayList<>();
    @Setter
    @Getter
    private GuiObjectWithSubObjects parent;

    private boolean clicked;
    private boolean selected, wasSelected;
    private final Vector2i selectPosition = new Vector2i();
    private InputBox inputBox;

    public InspectionEntry(InspectionPanel<T> inspectionPanel, int width, int height, String name, FontType fontType,
                           int fontSize, int stringYOffset) {
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
    public void update() {
        super.update();

        if (clicked && Engine.mouse.isLeftDown() && selectPosition.lengthSquared() > 0) {
            Vector2f mousePosition = Engine.mouse.getPosition();
            float moveThreshold = 40;
            if (mousePosition.distanceSquared(selectPosition.x, selectPosition.y) > moveThreshold) {
                onStartMoving();
            }
        }
    }

    private void onSelected() {
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

        Vector2f mousePosition = Engine.mouse.getPosition();
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
    public boolean onMouseLeftRelease() {
        clicked = false;

        if (!isMouseHover()) return false;

        Vector2f mousePosition = Engine.mouse.getPosition();
        int mouseX = (int) mousePosition.x;
        int selectOffsetX = canMaximize ? MINIMIZABLE_STRING_X_OFFSET : 0;
        if (canMaximize && mouseX >= x && mouseX < x + MINIMIZABLE_STRING_X_OFFSET) {
            if (maximized) {
                minimize();
            } else {
                maximize();
            }

            return true;
        } else if (selected) {
            if (mouseX >= x + selectOffsetX && mouseX < x + width) {
                if (wasSelected) {
                    inputBox = new InputBox(width - selectOffsetX, height, "", stringObject.getStringCache(), fontSize, 3,
                            stringYOffset, 300);
                    inputBox.setOnUnselectedRunnable(() -> {
                        gui.unregisterGuiObject(inputBox);
                        onNameChanged(inputBox.getString());
                        inputBox = null;
                    });
                    EditorTheme.setupInputBoxColors(inputBox);
                    inputBox.setString(getName());
                    subObjectsRepositionConsumer.setup(inputBox, selectOffsetX, 0);
                    gui.registerGuiObject(inputBox);
                    inputBox.enableTyping();
                    selected = false;
                } else {
                    onSelected();
                }

                return true;
            }
        }

        return false;
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

        Vector2f mousePosition = Engine.mouse.getPosition();
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
            guiRenderer.add(lastX, lastY, x, y, width, height, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f,
                    hoverColor.w);
        } else if (isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z,
                    hoverColor.w);
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
    public void addSubObject(AbstractGuiObject object) {
        super.addSubObject(object);
        setCanMaximize(true);
        if (object instanceof InspectionEntry<?> inspectionMinimizableGuiObject) {
            inspectionMinimizableGuiObject.setParent(this);
        }
    }

    @Override
    public void addSubObject(int index, AbstractGuiObject object) {
        super.addSubObject(index, object);
        setCanMaximize(true);
        if (object instanceof InspectionEntry<?> inspectionMinimizableGuiObject) {
            inspectionMinimizableGuiObject.setParent(this);
        }
    }

    @Override
    public void removeSubObject(AbstractGuiObject object) {
        super.removeSubObject(object);
        if (subObjects.isEmpty()) {
            setCanMaximize(false);
        }
    }

    @Nullable
    @Override
    public <COMPONENT_TYPE extends PropertiesHolder> COMPONENT_TYPE getComponentByType(Class<COMPONENT_TYPE> type) {
        for (int i = 0; i < components.size(); i++) {
            T t = components.get(i);
            if (t.getClass() == type) {
                return (COMPONENT_TYPE) t;
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