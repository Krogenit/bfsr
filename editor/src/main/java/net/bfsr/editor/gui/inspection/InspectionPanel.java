package net.bfsr.editor.gui.inspection;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.*;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.editor.gui.component.MinimizableGuiObject;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.util.MutableInt;
import org.joml.Vector2f;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.ColorScheme.TEXT_COLOR;
import static net.bfsr.editor.gui.ColorScheme.setupButtonColors;

public class InspectionPanel<T extends PropertiesHolder> {
    private static final long HOVER_TIME_FOR_MAXIMIZE = 500L;

    private final Gui gui;
    private final String name;
    private final GuiObjectsContainer objectsContainer;
    private final FontType fontType;
    private final int fontSize;
    private final int stringYOffset;
    private final int elementHeight = 20;

    @Setter
    private InspectionEntry<T> wantSelectObject;
    @Setter
    private boolean wantUnselect;
    @Setter
    @Getter
    private AbstractGuiObject movableObject;

    @Setter
    private Function<InspectionEntry<T>, Boolean> entryRightClickSupplier = (minimizableGuiObject) -> false;
    @Setter
    private Consumer<InspectionEntry<T>> onSelectConsumer = t -> {};

    private Button saveAllButton, addButton;
    private MinimizableGuiObject lastHoverObject, hoverObject;
    private long hoverTime;

    public InspectionPanel(Gui gui, String name, int width, FontType fontType, int fontSize, int stringYOffset) {
        this.gui = gui;
        this.name = name;
        this.objectsContainer = new GuiObjectsContainer(width, 16);
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.stringYOffset = stringYOffset;
    }

    public void initElements(int x, int y, Runnable saveAllRunnable, Supplier<InspectionEntry<T>> objectSupplier) {
        gui.registerGuiObject(new StringObject(fontType, name, fontSize, TEXT_COLOR.x, TEXT_COLOR.y, TEXT_COLOR.z, TEXT_COLOR.w).compile().atTopLeftCorner(x,
                y + fontType.getStringCache().getCenteredYOffset(name, elementHeight, fontSize) + stringYOffset));
        gui.registerGuiObject(objectsContainer.atTopLeftCorner(x, elementHeight).setHeightResizeFunction((width, height) -> Core.get().getScreenHeight() - elementHeight * 3));

        y -= elementHeight;
        saveAllButton = new Button(objectsContainer.getWidth(), elementHeight, "Save All", fontType, fontSize, stringYOffset, saveAllRunnable);
        gui.registerGuiObject(setupButtonColors(saveAllButton).atBottomLeftCorner(x, y));
        y -= elementHeight;
        addButton = new Button(objectsContainer.getWidth(), elementHeight, "Add", fontType, fontSize, stringYOffset, () -> {
            InspectionEntry<T> minimizableHolder = objectSupplier.get();
            addSubObject(minimizableHolder);
            updatePositions();
        });
        gui.registerGuiObject(setupButtonColors(addButton).atBottomLeftCorner(x, y));
    }

    public InspectionEntry<T> createEntry(String name) {
        InspectionEntry<T> entry = new InspectionEntry<>(this, objectsContainer.getWidth() - objectsContainer.getScrollWidth(), elementHeight, name, fontType, fontSize, stringYOffset);
        entry.setOnRightClickSupplier(() -> entryRightClickSupplier.apply(entry));
        return entry;
    }

    public InspectionEntry<T> createEntry() {
        return createEntry("Entry");
    }

    public InspectionEntry<T> createEntry(String name, T... objects) {
        InspectionEntry<T> entry = createEntry(name);
        for (int i = 0; i < objects.length; i++) {
            entry.addObject(objects[i]);
        }
        return entry;
    }

    public InspectionEntry<T> createEntry(T... objects) {
        return createEntry("Entry", objects);
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

            findHoverObjectToMaximize(gui.getGuiObjects());
        }
    }

    private <T extends GuiObject> void findHoverObjectToMaximize(List<T> guiObjects) {
        for (int i = 0; i < guiObjects.size(); i++) {
            T guiObject = guiObjects.get(i);
            if (guiObject.isMouseHover() && guiObject instanceof MinimizableGuiObject minimizableGuiObject && !minimizableGuiObject.isMaximized() && minimizableGuiObject.isCanMaximize()) {
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
                findHoverObjectToMaximize(guiObjectWithSubObjects1.getSubObjects());
            }
        }
    }

    private void calculateMinWidth(MutableInt width, GuiObjectWithSubObjects guiObjectWithSubObjects) {
        List<AbstractGuiObject> guiObjects = guiObjectWithSubObjects.getSubObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            AbstractGuiObject guiObject = guiObjects.get(i);

            if (guiObject instanceof InspectionMinimizableGuiObject<?> inspectionGuiObject) {
                width.set(Math.max(width.get(), inspectionGuiObject.getStringObject().getX() + inspectionGuiObject.getStringObject().getWidth() - objectsContainer.getX()));
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

        MutableInt width = new MutableInt(0);
        calculateMinWidth(width, objectsContainer);
        int smallOffsetX = 10;
        int panelWidth = width.get() + objectsContainer.getScrollWidth() + smallOffsetX;
        objectsContainer.setWidth(panelWidth);
        addButton.setStringXOffset(panelWidth / 2);
        addButton.setWidth(panelWidth);
        saveAllButton.setStringXOffset(panelWidth / 2);
        saveAllButton.setWidth(panelWidth);

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
            int x = movableObject.getX();
            int y = movableObject.getY();

            Vector2f position = Mouse.getPosition();

            movableObject.setPosition((int) position.x, (int) position.y);
            movableObject.update();
            movableObject.render();
            movableObject.setPosition(x, y);
            movableObject.update();
        }
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

                InspectionEntry<T> entry = findEntry(inspectionEntry, path);
                if (entry != null) {
                    return entry;
                }
            }
        }

        return null;
    }

    public boolean isInHierarchy(GuiObjectWithSubObjects hierarchy, InspectionMinimizableGuiObject<T> guiObject) {
        GuiObjectWithSubObjects parent = guiObject.getParent();
        while (parent != null) {
            if (parent == hierarchy) return true;

            if (parent instanceof InspectionMinimizableGuiObject<?> inspectionMinimizableGuiObject) {
                parent = inspectionMinimizableGuiObject.getParent();
            } else {
                return false;
            }
        }

        return false;
    }

    public InspectionMinimizableGuiObject<T> getMouseHoverObject() {
        return getMouseHoverObject(objectsContainer);
    }

    private InspectionMinimizableGuiObject<T> getMouseHoverObject(GuiObjectWithSubObjects guiObjectWithSubObjects) {
        List<AbstractGuiObject> subObjects = guiObjectWithSubObjects.getSubObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            AbstractGuiObject guiObject = subObjects.get(i);

            if (guiObject instanceof InspectionMinimizableGuiObject<?> inspectionHolder) {
                if (inspectionHolder.isIntersectsWithMouse()) {
                    return (InspectionMinimizableGuiObject<T>) inspectionHolder;
                }

                if (guiObject instanceof InspectionEntry<?> inspectionEntry && inspectionEntry.isMaximized()) {
                    InspectionMinimizableGuiObject<T> objectWithSubObjects = getMouseHoverObject(inspectionEntry);
                    if (objectWithSubObjects != null) {
                        return objectWithSubObjects;
                    }
                }
            }
        }

        return null;
    }

    public void addSubObject(InspectionMinimizableGuiObject<T> object) {
        object.setParent(objectsContainer);
        objectsContainer.addSubObject(object);
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

    public float getWidth() {
        return objectsContainer.getWidth();
    }
}