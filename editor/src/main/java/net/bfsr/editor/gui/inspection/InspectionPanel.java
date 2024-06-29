package net.bfsr.editor.gui.inspection;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.editor.gui.component.MinimizableGuiObject;
import net.bfsr.editor.gui.component.receive.DragTarget;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.util.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static net.bfsr.editor.gui.EditorTheme.SELECTION_BLUE_COLOR;
import static net.bfsr.editor.gui.EditorTheme.TEXT_COLOR;
import static net.bfsr.editor.gui.EditorTheme.setupButton;
import static net.bfsr.editor.gui.EditorTheme.setupScrollPane;

public class InspectionPanel<PROPERTIES_TYPE extends PropertiesHolder> extends Rectangle {
    private static final long HOVER_TIME_FOR_MAXIMIZE = 500L;

    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractGUIRenderer guiRenderer = renderer.guiRenderer;
    private final GuiManager guiManager = Core.get().getGuiManager();
    private final Gui gui;
    private final String name;
    @Getter
    private final ScrollPane scrollPane;
    private final FontType fontType;
    private final int fontSize;
    private final int stringOffsetY;
    private final int elementHeight = 20;
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

    public InspectionPanel(Gui gui, String name, int width, int height, FontType fontType, int fontSize, int stringOffsetY) {
        super(width, height);
        this.gui = gui;
        this.name = name;
        this.scrollPane = setupScrollPane(new ScrollPane(width, height - elementHeight, 16));
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.stringOffsetY = stringOffsetY;
        setWidthFunction((width1, height1) -> getPanelWidth()).updatePositionAndSize();
        add(new Label(fontType, name, fontSize, TEXT_COLOR.x, TEXT_COLOR.y, TEXT_COLOR.z, TEXT_COLOR.w).compileAtOrigin()
                .atTopLeft(0, fontType.getStringCache().getCenteredYOffset(name, elementHeight, fontSize) + stringOffsetY));
        add(scrollPane.atTopLeft(0, elementHeight).setWidthFunction((width1, height1) -> getPanelWidth())
                .setHeightFunction((width1, height1) -> this.height - elementHeight));
    }

    public void addBottomButton(int x, int y, String name, Runnable runnable) {
        Button button = new Button(scrollPane.getWidth(), elementHeight, name, fontType, fontSize, stringOffsetY, runnable);
        add(setupButton(button).atBottomLeft(x, y).setWidthFunction((width1, height1) -> getPanelWidth()));
        bottomButtons.add(button);
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
            int mouseY = (int) Engine.mouse.getPosition().y;
            if (mouseY < inspectionEntry.getSceneY() + exactObjectSelectionOffsetY) {
                GuiObject parent = inspectionEntry.getParent();
                List<GuiObject> guiObjects = parent.getGuiObjects();
                int index = guiObjects.indexOf(inspectionEntry);
                entry.getParent().remove(entry);
                parent.addAt(index, entry);

                if (parent == scrollPane) {
                    entry.setParent(scrollPane);
                }

                updatePositionAndSize();
            } else if (mouseY >= inspectionEntry.getSceneY() + elementHeight - exactObjectSelectionOffsetY) {
                GuiObject parent;
                if (inspectionEntry.isMaximized()) {
                    parent = inspectionEntry;
                } else {
                    parent = inspectionEntry.getParent();
                }

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

    private void calculateMinWidth(MutableInt width, GuiObject guiObject) {
        List<GuiObject> guiObjects = guiObject.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject1 = guiObjects.get(i);

            if (guiObject1 instanceof InspectionEntry<?> inspectionEntry) {
                width.set(Math.max(width.get(), inspectionEntry.getLabel().getSceneX() +
                        inspectionEntry.getLabel().getWidth() - scrollPane.getSceneX()));
                if (inspectionEntry.isMaximized()) {
                    calculateMinWidth(width, inspectionEntry);
                }
            }
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
            y += guiObject.getHeight();
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

    @Override
    public void render(AbstractGUIRenderer guiRenderer, int lastX, int lastY, int x, int y) {
        super.render(guiRenderer, lastX, lastY, x, y);
        renderMovableObject();
    }

    private void renderMovableObject() {
        if (movableObject == null) return;

        Vector2f position = Engine.mouse.getPosition();
        int mouseX = (int) position.x;
        int mouseY = (int) position.y;

        if (isIntersectsWithMouse()) {
            renderInsertingPreview(mouseY);
        }

        movableObject.getRenderer().render(mouseX, mouseY, mouseX, mouseY, movableObject.getWidth(), movableObject.getHeight());
    }

    private void renderInsertingPreview(int mouseY) {
        GuiObject guiObject = guiManager.getHoveredGuiObject();
        if (guiObject instanceof InspectionEntry<?> inspectionEntry) {
            int sceneX = inspectionEntry.getSceneX();
            int sceneY = inspectionEntry.getSceneY();
            int betweenObjectsLineHeight = 4;
            if (mouseY < sceneY + exactObjectSelectionOffsetY) {
                renderSelection(sceneX, sceneY - betweenObjectsLineHeight / 2, inspectionEntry.getWidth(), betweenObjectsLineHeight);
            } else if (mouseY >= sceneY + elementHeight - exactObjectSelectionOffsetY) {
                if (inspectionEntry.isMaximized() && inspectionEntry.getGuiObjects().size() > 0) {
                    GuiObject guiObject1 = inspectionEntry.getGuiObjects().get(0);
                    renderSelection(guiObject1.getSceneX(), sceneY + elementHeight - betweenObjectsLineHeight / 2, guiObject1.getWidth(),
                            betweenObjectsLineHeight);
                } else {
                    renderSelection(sceneX, sceneY + elementHeight - betweenObjectsLineHeight / 2, inspectionEntry.getWidth(),
                            betweenObjectsLineHeight);
                }
            } else if (inspectionEntry != movableObject &&
                    !isInHierarchy(movableObject, (InspectionEntry<PROPERTIES_TYPE>) inspectionEntry)) {
                renderSelection(sceneX, sceneY, inspectionEntry.getWidth());
                inspectionEntry.getRenderer()
                        .render(sceneX, sceneY, sceneX, sceneY, inspectionEntry.getWidth(), inspectionEntry.getHeight());
            }
        } else {
            int height = 0;
            List<GuiObject> subObjects = scrollPane.getGuiObjects();
            for (int i = 0; i < subObjects.size(); i++) {
                height += subObjects.get(i).getHeight();
            }

            int x1 = scrollPane.getSceneX();
            int y1 = scrollPane.getSceneY() + height;
            movableObject.getRenderer().render(x1, y1, x1, y1, movableObject.getWidth(), movableObject.getHeight());
        }
    }

    private void renderSelection(int x, int y, int width) {
        renderSelection(x, y, width, elementHeight);
    }

    private void renderSelection(int x, int y, int width, int height) {
        guiRenderer.add(x, y, width, height, SELECTION_BLUE_COLOR.x, SELECTION_BLUE_COLOR.y,
                SELECTION_BLUE_COLOR.z, SELECTION_BLUE_COLOR.w);
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

    private boolean isInHierarchy(GuiObject hierarchy, InspectionEntry<PROPERTIES_TYPE> guiObject) {
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
        calculateMinWidth(width, scrollPane);

        int smallOffsetX = 10;
        return width.get() + scrollPane.getScrollWidth() + smallOffsetX;
    }
}