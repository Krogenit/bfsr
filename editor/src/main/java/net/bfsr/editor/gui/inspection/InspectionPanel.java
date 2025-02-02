package net.bfsr.editor.gui.inspection;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Client;
import net.bfsr.editor.gui.component.receive.DragTarget;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.MinimizableGuiObject;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.util.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static net.bfsr.editor.gui.EditorTheme.TEXT_COLOR;
import static net.bfsr.editor.gui.EditorTheme.setupButton;
import static net.bfsr.editor.gui.EditorTheme.setupScrollPane;

public class InspectionPanel<PROPERTIES_TYPE extends PropertiesHolder> extends Rectangle {
    private static final long HOVER_TIME_FOR_MAXIMIZE = 500L;

    private final AbstractRenderer renderer = Engine.renderer;
    private final GuiManager guiManager = Client.get().getGuiManager();
    private final Gui gui;
    @Getter
    private final ScrollPane scrollPane;
    private final Font font;
    private final int fontSize;
    private final int stringOffsetY;
    @Getter
    private final int elementHeight = 20;
    @Getter
    private final int exactObjectSelectionOffsetY = 6;
    @Setter
    private @Nullable InspectionEntry<PROPERTIES_TYPE> wantSelectObject;
    @Setter
    private boolean wantUnselect;
    @Setter
    @Getter
    private @Nullable InspectionEntry<PROPERTIES_TYPE> movableObject;
    private Consumer<InspectionEntry<PROPERTIES_TYPE>> onSelectConsumer = t -> {};
    private @Nullable MinimizableGuiObject lastHoverObject, hoverObject;
    private long hoverTime;
    private final List<Button> bottomButtons = new ArrayList<>();

    public InspectionPanel(Gui gui, String name, int width, int height, Font font, int fontSize, int stringOffsetY) {
        super(width, height);
        this.gui = gui;
        this.scrollPane = setupScrollPane(new ScrollPane(width, height - elementHeight, 16));
        this.font = font;
        this.fontSize = fontSize;
        this.stringOffsetY = stringOffsetY;
        setWidthFunction((width1, height1) -> getPanelWidth()).updatePositionAndSize();
        Label label = new Label(font, name, fontSize, TEXT_COLOR.x, TEXT_COLOR.y, TEXT_COLOR.z, TEXT_COLOR.w);
        add(label.atBottomLeft(0, height - elementHeight + label.getCenteredOffsetY(elementHeight)));
        add(scrollPane.atBottomLeft(0, 0).setWidthFunction((width1, height1) -> getPanelWidth())
                .setHeightFunction((width1, height1) -> this.height - elementHeight));
        setRenderer(new InspectionPanelRenderer<>(this));
    }

    public void addBottomButton(int x, int y, String name, Runnable runnable) {
        Button button = new Button(scrollPane.getWidth(), elementHeight, name, font, fontSize, stringOffsetY, runnable);
        add(setupButton(button).atBottomLeft(x, y).setWidthFunction((width1, height1) -> getPanelWidth()));
        bottomButtons.add(button);

        scrollPane.atBottomLeft(0, bottomButtons.size() * elementHeight);
        scrollPane.setHeight(renderer.getScreenHeight() - elementHeight - bottomButtons.size() * elementHeight);
        scrollPane.setHeightFunction(
                (width1, height1) -> renderer.getScreenHeight() - elementHeight - bottomButtons.size() * elementHeight);
    }

    public void add(InspectionEntry<PROPERTIES_TYPE> entry) {
        scrollPane.add(entry);
        updatePositionAndSize();
    }

    public void removeEntry(InspectionEntry<PROPERTIES_TYPE> entry) {
        entry.getParent().remove(entry);
        updatePositionAndSize();
    }

    @Override
    public GuiObject mouseLeftClick() {
        GuiObject guiObject = super.mouseLeftClick();
        if (guiObject == scrollPane) {
            wantUnselect = true;
        }

        return guiObject;
    }

    @Override
    public GuiObject mouseLeftRelease() {
        GuiObject child = super.mouseLeftRelease();

        if (movableObject != null) {
            if (isIntersectsWithMouse()) {
                onEntryMoved(movableObject);
                child = this;
            } else {
                GuiObject hoveredGuiObject = guiManager.getHoveredGuiObject();
                if (hoveredGuiObject instanceof DragTarget dragTarget) {
                    if (dragTarget.canAcceptDraggable(movableObject)) {
                        dragTarget.acceptDraggable(movableObject);
                        child = this;
                    }
                }
            }

            movableObject = null;
        }

        return child;
    }

    private void onEntryMoved(InspectionEntry<PROPERTIES_TYPE> entry) {
        GuiObject guiObject = guiManager.getHoveredGuiObject();
        if (guiObject instanceof InspectionEntry<?> inspectionEntry) {
            int mouseY = (int) gui.getMousePosition().y;
            if (mouseY < inspectionEntry.getSceneY() + exactObjectSelectionOffsetY) {
                GuiObject parent = inspectionEntry.getParent();
                List<GuiObject> guiObjects = parent.getGuiObjects();
                int index = guiObjects.indexOf(inspectionEntry) + 1;
                entry.getParent().remove(entry);
                if (index >= guiObjects.size()) {
                    parent.add(entry);
                } else {
                    parent.addAt(index, entry);
                }

                if (parent == scrollPane) {
                    entry.setParent(scrollPane);
                }

                updatePositionAndSize();
            } else if (mouseY >= inspectionEntry.getSceneY() + inspectionEntry.getBaseHeight() - exactObjectSelectionOffsetY) {
                GuiObject parent;
                if (inspectionEntry.isMaximized()) {
                    parent = inspectionEntry;
                } else {
                    parent = inspectionEntry.getParent();
                }

                List<GuiObject> guiObjects = parent.getGuiObjects();
                int index = guiObjects.indexOf(inspectionEntry);
                entry.getParent().remove(entry);
                parent.addAt(index, entry);

                if (parent == scrollPane) {
                    entry.setParent(scrollPane);
                }

                updatePositionAndSize();
            } else if (inspectionEntry != movableObject &&
                    !isInHierarchy(movableObject, (InspectionEntry<PROPERTIES_TYPE>) inspectionEntry)) {
                entry.getParent().remove(entry);
                inspectionEntry.add(entry);
                inspectionEntry.tryMaximize();
                updatePositionAndSize();
            }
        } else {
            entry.getParent().remove(entry);
            add(entry);
            updatePositionAndSize();
        }
    }

    @Override
    public void update() {
        super.update();

        if (wantUnselect) {
            wantUnselect = false;
            if (wantSelectObject == null) {
                onSelectConsumer.accept(null);
            }
        } else if (wantSelectObject != null) {
            onSelectConsumer.accept(wantSelectObject);
            wantSelectObject = null;
        }

        if (movableObject != null) {
            lastHoverObject = hoverObject;
            hoverObject = null;

            findHoverObjectToMaximize(gui.getGuiObjects(), (int) Engine.mouse.getPosition().y);
            disableCurrentGuiObjectHover();
        }
    }

    /**
     * This if for correct rendering of target entry for movable object
     */
    private void disableCurrentGuiObjectHover() {
        if (isIntersectsWithMouse()) {
            GuiObject hoveredGuiObject = guiManager.getHoveredGuiObject();
            if (hoveredGuiObject != null) {
                hoveredGuiObject.setMouseHover(false);
            }
        }
    }

    private <GUI_OBJECT_TYPE extends GuiObject> void findHoverObjectToMaximize(List<GUI_OBJECT_TYPE> guiObjects, int mouseY) {
        for (int i = 0; i < guiObjects.size(); i++) {
            GUI_OBJECT_TYPE guiObject = guiObjects.get(i);
            if (guiObject.isIntersectsWithMouse() && guiObject instanceof MinimizableGuiObject minimizableGuiObject &&
                    !minimizableGuiObject.isMaximized() && minimizableGuiObject.isCanMaximize() &&
                    mouseY >= guiObject.getSceneY() + exactObjectSelectionOffsetY &&
                    mouseY < guiObject.getSceneY() + elementHeight - exactObjectSelectionOffsetY) {
                hoverObject = minimizableGuiObject;

                if (lastHoverObject != hoverObject) {
                    hoverTime = System.currentTimeMillis();
                }

                if (System.currentTimeMillis() - hoverTime > HOVER_TIME_FOR_MAXIMIZE) {
                    hoverObject.tryMaximize();
                }

                return;
            }

            findHoverObjectToMaximize(guiObject.getGuiObjects(), mouseY);
        }
    }

    @Override
    protected void onChildSizeChanged(GuiObject guiObject, int width, int height) {
        super.onChildSizeChanged(guiObject, width, height);
        updatePositionAndSize();
    }

    @Override
    public void updatePositionAndSize() {
        updatePositions();
        super.updatePositionAndSize();
    }

    private void updatePositions() {
        List<GuiObject> guiObjects = scrollPane.getGuiObjects();
        for (int i = 0, y = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject = guiObjects.get(i);
            guiObject.atTopLeft(0, y);
            y -= guiObject.getHeight();
        }

        int panelWidth = getPanelWidth();
        int stringXOffset = panelWidth / 2;
        for (int i = 0; i < bottomButtons.size(); i++) {
            Button button = bottomButtons.get(i);
            button.setStringXOffset(stringXOffset);
        }

        int maxWidth = scrollPane.getWidth() - scrollPane.getScrollWidth();
        for (int i = 0; i < guiObjects.size(); i++) {
            updateWidth(guiObjects.get(i), maxWidth);
        }
    }

    private void updateWidth(GuiObject guiObject, int width) {
        guiObject.setWidthFunction((width1, height1) -> width);

        List<GuiObject> guiObjects = guiObject.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            updateWidth(guiObjects.get(i), width - MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET);
        }
    }

    public InspectionEntry<PROPERTIES_TYPE> findEntry(String path) {
        return findEntry(scrollPane, path);
    }

    public @Nullable InspectionEntry<PROPERTIES_TYPE> findEntry(GuiObject guiObject, String path) {
        List<GuiObject> subObjects = guiObject.getGuiObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            GuiObject simpleGuiObject = subObjects.get(i);
            if (simpleGuiObject instanceof InspectionEntry<?> inspectionEntry) {
                if (inspectionEntry.getName().equals(path)) {
                    return (InspectionEntry<PROPERTIES_TYPE>) inspectionEntry;
                }
            }
        }

        return null;
    }

    boolean isInHierarchy(GuiObject hierarchy, InspectionEntry<PROPERTIES_TYPE> guiObject) {
        GuiObject parent = guiObject.getParent();
        while (parent != null) {
            if (parent == hierarchy) return true;

            parent = parent.getParent();
        }

        return false;
    }

    public void sortFolders() {
        scrollPane.getGuiObjects().sort(Comparator.comparing(o -> ((MinimizableGuiObject) o).getName()));
        updatePositionAndSize();
    }

    @Override
    public InspectionPanel<PROPERTIES_TYPE> setRightClickRunnable(Runnable runnable) {
        scrollPane.setRightClickRunnable(runnable);
        return this;
    }

    public InspectionPanel<PROPERTIES_TYPE> setOnSelectConsumer(Consumer<InspectionEntry<PROPERTIES_TYPE>> onSelectConsumer) {
        this.onSelectConsumer = onSelectConsumer;
        return this;
    }

    private int getPanelWidth() {
        int minWidth = 100;
        MutableInt width = new MutableInt(minWidth);
        calculateMinWidth(width, scrollPane, 0);

        int smallOffsetX = 10;
        return width.get() + scrollPane.getScrollWidth() + smallOffsetX;
    }

    private void calculateMinWidth(MutableInt width, GuiObject guiObject, int offsetX) {
        List<GuiObject> guiObjects = guiObject.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject1 = guiObjects.get(i);

            if (guiObject1 instanceof InspectionEntry<?> inspectionEntry) {
                width.set(Math.max(width.get(), offsetX + inspectionEntry.getLabel().getWidth() - scrollPane.getSceneX()));
                if (inspectionEntry.isMaximized()) {
                    calculateMinWidth(width, inspectionEntry, offsetX + 20);
                }
            }
        }
    }
}