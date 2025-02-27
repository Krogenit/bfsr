package net.bfsr.editor.gui.inspection;

import lombok.Getter;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.component.ComponentHolder;
import net.bfsr.editor.gui.renderer.InspectionEntryRenderer;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.component.MinimizableGuiObject;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.font.glyph.Font;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.setup;

public class InspectionEntry<T extends PropertiesHolder> extends MinimizableGuiObject implements ComponentHolder {
    private final AbstractMouse mouse = Engine.getMouse();

    private final InspectionPanel<T> inspectionPanel;
    private final List<T> components = new ArrayList<>();
    private boolean clicked;
    @Getter
    private boolean selected;
    private boolean wasSelected;
    private final Vector2i selectPosition = new Vector2i();
    private @Nullable InputBox inputBox;

    public InspectionEntry(InspectionPanel<T> inspectionPanel, int width, int height, String name, Font font, int fontSize,
                           int stringOffsetY) {
        super(width, height, name, font, fontSize, stringOffsetY);
        this.inspectionPanel = inspectionPanel;
        setCanMaximize(false);
        setRenderer(new InspectionEntryRenderer(this));
        setup(this);
        setLeftClickConsumer((mouseX, mouseY) -> {
            selectPosition.set(mouseX, mouseY);
            clicked = true;

            if (!selected && inputBox == null) {
                int selectOffsetX = canMaximize ? MINIMIZABLE_STRING_X_OFFSET : 0;
                int sceneX = getSceneX();
                if (mouseX >= sceneX + selectOffsetX && mouseX < sceneX + width) {
                    select();
                }
            }
        });
        setLeftReleaseConsumer((mouseX, mouseY) -> {
            int selectOffsetX = canMaximize ? MINIMIZABLE_STRING_X_OFFSET : 0;
            int sceneX = getSceneX();
            if (canMaximize && mouseX >= sceneX && mouseX < sceneX + MINIMIZABLE_STRING_X_OFFSET) {
                if (maximized) {
                    maximized = false;
                    minimize();
                } else {
                    maximized = true;
                    maximize();
                }
            } else if (selected) {
                if (mouseX >= sceneX + selectOffsetX && mouseX < sceneX + width) {
                    if (wasSelected) {
                        inputBox = new InputBox(width - selectOffsetX, height, "", font, fontSize, 3, this.stringOffsetY, 300);
                        inputBox.setOnUnselectedRunnable(() -> {
                            removeNonConcealable(inputBox);
                            onNameChanged(inputBox.getString());
                            inputBox = null;
                        });
                        EditorTheme.setupInputBox(inputBox);
                        inputBox.setString(getName());
                        addNonConcealable(inputBox.atBottomLeft(selectOffsetX, getHeight() - getBaseHeight()));
                        inputBox.enableTyping();
                        selected = false;
                    } else {
                        onSelected();
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public GuiObject mouseLeftClick(int mouseX, int mouseY) {
        wasSelected = selected;

        GuiObject hovered = inspectionPanel.getHovered(null, mouseX, mouseY);
        if (!isMouseHover() && selected && (hovered == inspectionPanel.getScrollPane() || hovered instanceof InspectionEntry)) {
            unselect();
        }

        return super.mouseLeftClick(mouseX, mouseY);
    }

    @Override
    public GuiObject mouseRightClick(int mouseX, int mouseY) {
        GuiObject guiObject = super.mouseRightClick(mouseX, mouseY);
        wasSelected = selected;

        if (!isMouseHover() && (inputBox == null || !inputBox.isIntersectsWithMouse(mouseX, mouseY))) {
            if (selected) {
                unselect();
            }

            return guiObject;
        }

        if (!selected && inputBox == null) {
            int selectOffsetX = canMaximize ? MINIMIZABLE_STRING_X_OFFSET : 0;
            int sceneX = getSceneX();
            if (mouseX >= sceneX + selectOffsetX && mouseX < sceneX + width) {
                selected = true;
            }
        }

        return guiObject;
    }

    @Override
    public GuiObject mouseLeftRelease(int mouseX, int mouseY) {
        clicked = false;
        return super.mouseLeftRelease(mouseX, mouseY);
    }

    @Override
    public void remove() {
        super.remove();
        selected = false;
    }

    @Override
    protected void onStartMoving() {
        inspectionPanel.setMovableObject(this);
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);

        if (clicked && mouse.isLeftDown() && selectPosition.lengthSquared() > 0) {
            float moveThreshold = 40;
            if (selectPosition.distanceSquared(mouseX, mouseY) > moveThreshold) {
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
    public void addHideable(GuiObject object) {
        super.addHideable(object);
        setCanMaximize(true);
    }

    @Override
    public void removeHideable(GuiObject object) {
        super.removeHideable(object);
        if (hideableObjects.isEmpty()) {
            setCanMaximize(false);
        }
    }

    @SuppressWarnings("unchecked")
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
}