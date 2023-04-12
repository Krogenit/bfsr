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
import net.bfsr.property.PropertiesHolder;
import org.joml.Vector2f;
import org.joml.Vector2i;

import static net.bfsr.editor.gui.ColorScheme.setupColors;

public class InspectionMinimizableGuiObject<T extends PropertiesHolder> extends MinimizableGuiObject {
    protected final InspectionPanel<T> inspectionPanel;

    private boolean moving;
    private boolean selected, wasSelected;
    protected final Vector2i selectPosition = new Vector2i();
    private InputBox inputBox;
    @Setter
    @Getter
    protected GuiObjectWithSubObjects parent;

    public InspectionMinimizableGuiObject(InspectionPanel<T> inspectionPanel, int width, int height, String name, FontType fontType, int fontSize, int stringYOffset) {
        super(width, height, name, fontType, fontSize, stringYOffset);
        this.inspectionPanel = inspectionPanel;
        setupColors(this);
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
        if (selected && guiObject != inputBox) {
            unselect();
        }
    }

    @Override
    public void onOtherGuiObjectMouseRightClick(GuiObject guiObject) {
        if (selected && guiObject != inputBox) {
            unselect();
        }
    }

    @Override
    protected void onMoved() {
        if (inspectionPanel.isIntersectsWithMouse()) {
            InspectionMinimizableGuiObject<T> inspectionGuiObject = inspectionPanel.getMouseHoverObject();
            if (inspectionGuiObject != null) {
                if (inspectionGuiObject != this && inspectionGuiObject.isCanMaximize() && !inspectionPanel.isInHierarchy(this, inspectionGuiObject)) {
                    parent.removeSubObject(this);
                    inspectionGuiObject.addSubObject(this);
                    inspectionGuiObject.maximize();
                    inspectionPanel.updatePositions();
                }
            } else {
                parent.removeSubObject(this);
                inspectionPanel.addSubObject(this);
                inspectionPanel.updatePositions();
            }
        }

        inspectionPanel.setMovableObject(null);
    }

    @Override
    public void update() {
        super.update();

        if (selected && Mouse.isLeftDown() && selectPosition.lengthSquared() > 0 && !gui.isContextMenuOpened()) {
            Vector2f mousePosition = Mouse.getPosition();
            float moveThreshold = 40;
            if (mousePosition.distanceSquared(selectPosition.x, selectPosition.y) > moveThreshold) {
                onStartMoving();
                moving = true;
            }
        }
    }

    protected void onSelected() {}

    protected void onUnselected() {}

    private void select() {
        selected = true;
        onSelected();
    }

    private void unselect() {
        selected = false;
        onUnselected();
    }

    @Override
    public void onContextMenuClosed() {
        Vector2f mousePosition = Mouse.getPosition();
        selectPosition.set((int) mousePosition.x, (int) mousePosition.y);
    }

    @Override
    public boolean onMouseLeftClick() {
        wasSelected = selected;

        if (!isMouseHover() && (inputBox == null || !inputBox.isMouseHover())) {
            if (selected) {
                unselect();
            }

            return false;
        }

        Vector2f mousePosition = Mouse.getPosition();
        int mouseX = (int) mousePosition.x;
        int mouseY = (int) mousePosition.y;
        selectPosition.set(mouseX, mouseY);

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
        if (moving) {
            onMoved();
            moving = false;
            return;
        }

        selectPosition.set(0, 0);

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
        } else if (wasSelected && selected && mouseX >= x + selectOffsetX && mouseX < x + width) {
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
            inputBox.setTyping(true);
            selected = false;
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
                select();
            }
        }

        return super.onMouseRightClick();
    }

    @Override
    public void addSubObject(AbstractGuiObject guiObject) {
        super.addSubObject(guiObject);
        if (guiObject instanceof InspectionMinimizableGuiObject<?> inspectionMinimizableGuiObject) {
            inspectionMinimizableGuiObject.setParent(this);
        }
    }

    @Override
    protected void renderBase() {
        if (selected) {
            GUIRenderer.get().add(lastX, lastY, x, y, width, height, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
        } else if (isMouseHover()) {
            GUIRenderer.get().add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }
}