package net.bfsr.editor.gui.inspection;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.editor.gui.component.MinimizableGuiObject;
import net.bfsr.editor.gui.component.receive.DragTarget;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.gui.object.*;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.util.MutableInt;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.*;

public class InspectionPanel<T extends PropertiesHolder> {
    private static final long HOVER_TIME_FOR_MAXIMIZE = 500L;

    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractGUIRenderer guiRenderer = renderer.guiRenderer;
    private final Gui gui;
    private final String name;
    private final GuiObjectsContainer objectsContainer;
    private final FontType fontType;
    private final int fontSize;
    private final int stringYOffset;
    private final int minWidth = 100;
    private final int elementHeight = 20;
    private final int betweenObjectsLineHeight = 4;
    private final int exactObjectSelectionOffsetY = 6;

    @Setter
    private InspectionEntry<T> wantSelectObject;
    @Setter
    private boolean wantUnselect;
    @Setter
    @Getter
    private InspectionEntry<T> movableObject;

    @Setter
    private Function<InspectionEntry<T>, Boolean> entryRightClickSupplier = (minimizableGuiObject) -> false;
    @Setter
    private Consumer<InspectionEntry<T>> onSelectConsumer = t -> {};

    private MinimizableGuiObject lastHoverObject, hoverObject;
    private long hoverTime;
    private final List<Button> bottomButtons = new ArrayList<>();
    private final SimpleGuiObject background = new SimpleGuiObject();

    public InspectionPanel(Gui gui, String name, int width, FontType fontType, int fontSize, int stringYOffset) {
        this.gui = gui;
        this.name = name;
        this.objectsContainer = new GuiObjectsContainer(gui, width, 16);
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.stringYOffset = stringYOffset;
    }

    public void initElements(int x, int y) {
        gui.registerGuiObject(
                background.setAllColors(BACKGROUND_COLOR.x, BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w)
                        .setWidthResizeFunction((integer, integer2) -> getWidth())
                        .setHeightResizeFunction((integer, integer2) -> Engine.renderer.getScreenHeight()));
        gui.registerGuiObject(
                new StringObject(fontType, name, fontSize, TEXT_COLOR.x, TEXT_COLOR.y, TEXT_COLOR.z, TEXT_COLOR.w).compile()
                        .atTopLeftCorner(x,
                                y + fontType.getStringCache().getCenteredYOffset(name, elementHeight, fontSize) + stringYOffset));
        gui.registerGuiObject(objectsContainer.atTopLeftCorner(x, elementHeight).setHeightResizeFunction(
                (width, height) -> renderer.getScreenHeight() - elementHeight)
        );
    }

    public void addBottomButton(int x, int y, String name, Runnable runnable) {
        Button button = new Button(objectsContainer.getWidth(), elementHeight, name, fontType, fontSize, stringYOffset, runnable);
        gui.registerGuiObject(setupButtonColors(button).atBottomLeftCorner(x, y));
        bottomButtons.add(button);
        objectsContainer.setHeight(renderer.getScreenHeight() - elementHeight - bottomButtons.size() * elementHeight);
        objectsContainer.setHeightResizeFunction((width, height) -> renderer.getScreenHeight() - elementHeight -
                bottomButtons.size() * elementHeight);
    }

    public InspectionEntry<T> createEntry(String name) {
        InspectionEntry<T> entry = new InspectionEntry<>(this, objectsContainer.getWidth() - objectsContainer.getScrollWidth(),
                elementHeight, name, fontType, fontSize, stringYOffset);
        entry.setOnRightClickSupplier(() -> entryRightClickSupplier.apply(entry));
        return entry;
    }

    public InspectionEntry<T> createEntry() {
        return createEntry("Entry");
    }

    public InspectionEntry<T> createEntry(String name, T component) {
        InspectionEntry<T> entry = createEntry(name);
        entry.addComponent(component);
        return entry;
    }

    public void onMouseLeftClick() {
        List<GuiObject> guiObjects = objectsContainer.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            if (guiObjects.get(i).isMouseHover()) {
                return;
            }
        }

        if (isMouseHover()) {
            wantUnselect = true;
        }
    }

    public boolean onMouseLeftRelease() {
        boolean leftRelease = false;

        if (movableObject != null) {
            if (isIntersectsWithMouse()) {
                onEntryMoved(movableObject);
                leftRelease = true;
            } else {
                GuiObject hoveredGuiObject = gui.getHoveredGuiObject();
                if (hoveredGuiObject instanceof DragTarget dragTarget) {
                    if (dragTarget.canAcceptDraggable(movableObject)) {
                        dragTarget.acceptDraggable(movableObject);
                        leftRelease = true;
                    }
                }
            }

            setMovableObject(null);
        }

        return leftRelease;
    }

    public void onEntryMoved(InspectionEntry<T> entry) {
        InspectionEntry<T> inspectionGuiObject = getMouseHoverObject();
        if (inspectionGuiObject != null) {
            int mouseY = (int) Engine.mouse.getPosition().y;
            if (mouseY < inspectionGuiObject.getY() + exactObjectSelectionOffsetY) {
                GuiObjectWithSubObjects parent = inspectionGuiObject.getParent();
                List<AbstractGuiObject> subObjects = parent.getSubObjects();
                int index = subObjects.indexOf(inspectionGuiObject);
                entry.getParent().removeSubObject(entry);
                parent.addSubObject(index, entry);
                if (parent == objectsContainer) {
                    entry.setParent(objectsContainer);
                }
                updatePositions();
            } else if (mouseY >= inspectionGuiObject.getY() + elementHeight - exactObjectSelectionOffsetY) {
                GuiObjectWithSubObjects parent;
                if (inspectionGuiObject.isMaximized()) {
                    parent = inspectionGuiObject;
                } else {
                    parent = inspectionGuiObject.getParent();
                }
                List<AbstractGuiObject> subObjects = parent.getSubObjects();
                int index = subObjects.indexOf(inspectionGuiObject) + 1;
                entry.getParent().removeSubObject(entry);
                if (index >= subObjects.size()) {
                    parent.addSubObject(entry);
                } else {
                    parent.addSubObject(index, entry);
                }
                if (parent == objectsContainer) {
                    entry.setParent(objectsContainer);
                }
                updatePositions();
            } else if (inspectionGuiObject != movableObject && !isInHierarchy(movableObject, inspectionGuiObject)) {
                entry.getParent().removeSubObject(entry);
                inspectionGuiObject.addSubObject(entry);
                inspectionGuiObject.maximize();
                updatePositions();
            }
        } else {
            entry.getParent().removeSubObject(entry);
            addSubObject(entry);
            updatePositions();
        }
    }

    public void update() {
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

    private void disableCurrentGuiObjectHover() {
        if (isIntersectsWithMouse()) {
            GuiObject hoveredGuiObject = gui.getHoveredGuiObject();
            if (hoveredGuiObject != null) {
                hoveredGuiObject.setMouseHover(false);
            }
        }
    }

    private <T extends GuiObject> void findHoverObjectToMaximize(List<T> guiObjects, int mouseY) {
        for (int i = 0; i < guiObjects.size(); i++) {
            T guiObject = guiObjects.get(i);
            if (guiObject.isMouseHover() && guiObject instanceof MinimizableGuiObject minimizableGuiObject &&
                    !minimizableGuiObject.isMaximized() && minimizableGuiObject.isCanMaximize()
                    && mouseY >= guiObject.getY() + exactObjectSelectionOffsetY &&
                    mouseY < guiObject.getY() + elementHeight - exactObjectSelectionOffsetY) {
                hoverObject = minimizableGuiObject;

                if (lastHoverObject != hoverObject) {
                    hoverTime = System.currentTimeMillis();
                }

                if (System.currentTimeMillis() - hoverTime > HOVER_TIME_FOR_MAXIMIZE) {
                    hoverObject.maximize();
                }

                return;
            }

            if (guiObject instanceof GuiObjectWithSubObjects guiObjectWithSubObjects1) {
                findHoverObjectToMaximize(guiObjectWithSubObjects1.getSubObjects(), mouseY);
            }
        }
    }

    private void calculateMinWidth(MutableInt width, GuiObjectWithSubObjects guiObjectWithSubObjects) {
        List<AbstractGuiObject> guiObjects = guiObjectWithSubObjects.getSubObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            AbstractGuiObject guiObject = guiObjects.get(i);

            if (guiObject instanceof InspectionEntry<?> inspectionGuiObject) {
                width.set(Math.max(width.get(),
                        inspectionGuiObject.getStringObject().getX() + inspectionGuiObject.getStringObject().getWidth() -
                                objectsContainer.getX()));
            }

            if (guiObject instanceof InspectionEntry<?> inspectionEntry && inspectionEntry.isMaximized()) {
                calculateMinWidth(width, inspectionEntry);
            }
        }
    }

    public void updatePositions() {
        int x = 0;
        int y = elementHeight;

        List<AbstractGuiObject> guiObjects = objectsContainer.getSubObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            AbstractGuiObject guiObject = guiObjects.get(i);
            guiObject.atTopLeftCorner(x, y);
            guiObject.updatePositionAndSize();
            y += guiObject.getHeight();
        }

        objectsContainer.updateScrollObjectsY();

        MutableInt width = new MutableInt(minWidth);
        calculateMinWidth(width, objectsContainer);
        int smallOffsetX = 10;
        int panelWidth = width.get() + objectsContainer.getScrollWidth() + smallOffsetX;
        objectsContainer.setWidth(panelWidth);
        background.setWidth(panelWidth);

        int stringXOffset = panelWidth / 2;
        for (int i = 0; i < bottomButtons.size(); i++) {
            Button button = bottomButtons.get(i);
            button.setStringXOffset(stringXOffset);
            button.setWidth(panelWidth);
        }

        int maxContainerX = objectsContainer.getX() + objectsContainer.getWidth() - objectsContainer.getScrollWidth();

        for (int i = 0; i < guiObjects.size(); i++) {
            AbstractGuiObject guiObject = guiObjects.get(i);
            updateWidth(guiObject, maxContainerX);
        }
    }

    private void updateWidth(AbstractGuiObject guiObject, int maxContainerX) {
        int objectRightPosX = guiObject.getX() + guiObject.getWidth();
        if (objectRightPosX > maxContainerX) {
            guiObject.setWidth(guiObject.getWidth() - (objectRightPosX - maxContainerX));
        } else if (objectRightPosX < maxContainerX) {
            guiObject.setWidth(guiObject.getWidth() + (maxContainerX - objectRightPosX));
        }

        if (guiObject instanceof GuiObjectWithSubObjects guiObjectWithSubObjects) {
            List<AbstractGuiObject> subObjects = guiObjectWithSubObjects.getSubObjects();
            for (int i = 0; i < subObjects.size(); i++) {
                updateWidth(subObjects.get(i), maxContainerX);
            }
        }
    }

    public void render() {
        if (movableObject != null) {
            renderMovableObject();
        }
    }

    private void renderMovableObject() {
        int x = movableObject.getX();
        int y = movableObject.getY();

        Vector2f position = Engine.mouse.getPosition();
        int mouseX = (int) position.x;
        int mouseY = (int) position.y;

        if (isIntersectsWithMouse()) {
            renderInsertingPreview(mouseY);
        }

        movableObject.setPosition(mouseX, mouseY);
        movableObject.update();
        movableObject.render();
        movableObject.setPosition(x, y);
        movableObject.update();
    }

    private void renderInsertingPreview(int mouseY) {
        InspectionEntry<T> inspectionGuiObject = getMouseHoverObject();
        if (inspectionGuiObject != null) {
            if (mouseY < inspectionGuiObject.getY() + exactObjectSelectionOffsetY) {
                renderSelection(inspectionGuiObject.getX(), inspectionGuiObject.getY() - betweenObjectsLineHeight / 2,
                        inspectionGuiObject.getWidth(), betweenObjectsLineHeight);
            } else if (mouseY >= inspectionGuiObject.getY() + elementHeight - exactObjectSelectionOffsetY) {
                if (inspectionGuiObject.isMaximized() && inspectionGuiObject.getSubObjects().size() > 0) {
                    AbstractGuiObject guiObject = inspectionGuiObject.getSubObjects().get(0);
                    renderSelection(guiObject.getX(), inspectionGuiObject.getY() + elementHeight - betweenObjectsLineHeight / 2,
                            guiObject.getWidth(), betweenObjectsLineHeight);
                } else {
                    renderSelection(inspectionGuiObject.getX(),
                            inspectionGuiObject.getY() + elementHeight - betweenObjectsLineHeight / 2,
                            inspectionGuiObject.getWidth(), betweenObjectsLineHeight);
                }
            } else if (inspectionGuiObject != movableObject && !isInHierarchy(movableObject, inspectionGuiObject)) {
                renderSelection(inspectionGuiObject.getX(), inspectionGuiObject.getY(), inspectionGuiObject.getWidth());
                inspectionGuiObject.setMouseHover(false);
                inspectionGuiObject.render();
            }
        } else {
            int height = 0;
            List<AbstractGuiObject> subObjects = objectsContainer.getSubObjects();
            for (int i = 0; i < subObjects.size(); i++) {
                height += subObjects.get(i).getHeight();
            }
            int x1 = objectsContainer.getX();
            int y1 = objectsContainer.getY() + height;
            int x = movableObject.getX();
            int y = movableObject.getY();
            movableObject.setPosition(x1, y1);
            movableObject.update();
            movableObject.render();
            movableObject.setPosition(x, y);
            movableObject.update();
        }
    }

    private void renderSelection(int x, int y, int width) {
        renderSelection(x, y, width, elementHeight);
    }

    private void renderSelection(int x, int y, int width, int height) {
        guiRenderer.add(x, y, width, height, SELECTION_BLUE_COLOR.x, SELECTION_BLUE_COLOR.y,
                SELECTION_BLUE_COLOR.z, SELECTION_BLUE_COLOR.w);
    }

    public InspectionEntry<T> findEntry(String path) {
        return findEntry(objectsContainer, path);
    }

    public InspectionEntry<T> findEntry(GuiObjectWithSubObjects guiObjectWithSubObjects, String path) {
        List<AbstractGuiObject> subObjects = guiObjectWithSubObjects.getSubObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            AbstractGuiObject abstractGuiObject = subObjects.get(i);
            if (abstractGuiObject instanceof InspectionEntry<?> inspectionEntry) {
                if (inspectionEntry.getName().equals(path)) {
                    return (InspectionEntry<T>) inspectionEntry;
                }
            }
        }

        return null;
    }

    public boolean isInHierarchy(GuiObjectWithSubObjects hierarchy, InspectionEntry<T> guiObject) {
        GuiObjectWithSubObjects parent = guiObject.getParent();
        while (parent != null) {
            if (parent == hierarchy) return true;

            if (parent instanceof InspectionEntry<?> inspectionMinimizableGuiObject) {
                parent = inspectionMinimizableGuiObject.getParent();
            } else {
                return false;
            }
        }

        return false;
    }

    public InspectionEntry<T> getMouseHoverObject() {
        return getMouseHoverObject(objectsContainer);
    }

    private InspectionEntry<T> getMouseHoverObject(GuiObjectWithSubObjects guiObjectWithSubObjects) {
        List<AbstractGuiObject> subObjects = guiObjectWithSubObjects.getSubObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            AbstractGuiObject guiObject = subObjects.get(i);

            if (guiObject instanceof InspectionEntry<?> inspectionEntry) {
                if (inspectionEntry.isIntersectsWithMouse()) {
                    return (InspectionEntry<T>) inspectionEntry;
                }

                if (inspectionEntry.isMaximized()) {
                    InspectionEntry<T> objectWithSubObjects = getMouseHoverObject(inspectionEntry);
                    if (objectWithSubObjects != null) {
                        return objectWithSubObjects;
                    }
                }
            }
        }

        return null;
    }

    public void addSubObject(InspectionEntry<T> object) {
        object.setParent(objectsContainer);
        objectsContainer.addSubObject(object);
    }

    public void sortFolders() {
        objectsContainer.getSubObjects().sort(Comparator.comparing(o -> ((MinimizableGuiObject) o).getName()));
    }

    public void setRightClickSupplier(Supplier<Boolean> supplier) {
        objectsContainer.setOnRightClickSupplier(supplier);
    }

    public boolean isMouseHover() {
        return objectsContainer.isMouseHover();
    }

    public boolean isIntersectsWithMouse() {
        return objectsContainer.isIntersectsWithMouse();
    }

    public int getWidth() {
        return objectsContainer.getWidth();
    }
}