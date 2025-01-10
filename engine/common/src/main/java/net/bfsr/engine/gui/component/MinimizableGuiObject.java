package net.bfsr.engine.gui.component;

import lombok.Getter;
import net.bfsr.engine.gui.renderer.MinimizableGuiObjectRenderer;
import net.bfsr.engine.renderer.font.Font;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class MinimizableGuiObject extends GuiObject {
    public static final int MINIMIZABLE_STRING_X_OFFSET = 20;
    protected static final int STATIC_STRING_X_OFFSET = 6;

    @Getter
    protected boolean maximized;
    @Getter
    protected final Label label;
    protected final Font font;
    protected final int fontSize;
    private final int stringOffsetX, minimizableStringOffsetX;
    protected final int stringOffsetY;
    @Getter
    protected boolean canMaximize = true;
    protected final List<GuiObject> hideableObjects = new ArrayList<>();
    @Getter
    protected final int baseHeight;
    private final int hideableObjectsOffsetX;

    public MinimizableGuiObject(int width, int height, String name, Font font, int fontSize, int stringOffsetX, int stringOffsetY,
                                int minimizableStringOffsetX, int hideableObjectsOffsetX) {
        super(width, height);
        this.baseHeight = height;
        this.font = font;
        this.fontSize = fontSize;
        this.label = new Label(font, name, fontSize);
        this.stringOffsetX = stringOffsetX;
        this.stringOffsetY = stringOffsetY;
        addNonConcealable(label.atBottomLeft(this::getStringOffsetX,
                () -> label.getCenteredOffsetY(height) + this.height - baseHeight));
        this.minimizableStringOffsetX = minimizableStringOffsetX;
        this.hideableObjectsOffsetX = hideableObjectsOffsetX;
        setRenderer(new MinimizableGuiObjectRenderer(this));
        setLeftReleaseRunnable(() -> {
            if (maximized) {
                maximized = false;
                minimize();
            } else if (canMaximize) {
                maximized = true;
                maximize();
            }
        });
    }

    public MinimizableGuiObject(int width, int height, String name, Font font, int fontSize, int stringOffsetY) {
        this(width, height, name, font, fontSize, STATIC_STRING_X_OFFSET, stringOffsetY, MINIMIZABLE_STRING_X_OFFSET,
                MINIMIZABLE_STRING_X_OFFSET);
    }

    public void tryMaximize() {
        if (canMaximize && !maximized) {
            maximized = true;
            maximize();
        }
    }

    protected void maximize() {
        updateHeight();
        addHideable();
        updatePositionAndSize();
    }

    protected void minimize() {
        updateHeight();
        removeHideable();
        updatePositionAndSize();
    }

    protected void updateHeight() {
        if (maximized) {
            int height = baseHeight;
            for (int i = 0; i < hideableObjects.size(); i++) {
                height += hideableObjects.get(i).getHeight();
            }

            setHeight(height);
        } else {
            setHeight(baseHeight);
        }
    }

    protected void onNameChanged(String name) {
        label.setString(name);
    }

    protected void onStartMoving() {}

    private void addHideable() {
        for (int i = 0; i < hideableObjects.size(); i++) {
            super.add(hideableObjects.get(i));
        }
    }

    private void removeHideable() {
        for (int i = 0; i < hideableObjects.size(); i++) {
            super.remove(hideableObjects.get(i));
        }
    }

    protected void addHideable(GuiObject object) {
        hideableObjects.add(object);
        onHideableAdded(object);
    }

    private void addHideableAt(int index, GuiObject object) {
        hideableObjects.add(index, object);
        onHideableAdded(object);
    }

    private int addHideableBefore(GuiObject object, GuiObject beforeObject) {
        int index = hideableObjects.indexOf(beforeObject);
        if (index >= 0) {
            addHideableAt(index, object);
            return index;
        } else {
            throw new RuntimeException("Failed to add hideable gui object " + object + " before " + beforeObject);
        }
    }

    private void onHideableAdded(GuiObject object) {
        if (maximized) {
            super.add(object);
            updateHeight();
        }

        updatePositionAndSize();
    }

    protected void removeHideable(GuiObject object) {
        hideableObjects.remove(object);
        super.remove(object);
        updateHeight();
        updatePositionAndSize();
    }

    protected void addNonConcealable(GuiObject guiObject) {
        super.add(guiObject);
    }

    protected void removeNonConcealable(GuiObject guiObject) {
        super.remove(guiObject);
    }

    @Override
    public void add(GuiObject guiObject) {
        addHideable(guiObject);
        guiObject.setParent(this);
    }

    @Override
    public void addAt(int index, GuiObject guiObject) {
        addHideableAt(index, guiObject);
        guiObject.setParent(this);
    }

    @Override
    public int addBefore(GuiObject guiObject, GuiObject beforeObject) {
        int index = addHideableBefore(guiObject, beforeObject);
        guiObject.setParent(this);
        return index;
    }

    @Override
    public void remove(GuiObject guiObject) {
        removeHideable(guiObject);
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        updateConcealableObjectsPositions();
        super.updatePositionAndSize(width, height);
    }

    public void updateConcealableObjectsPositions() {
        int height = -baseHeight;

        for (int i = 0; i < hideableObjects.size(); i++) {
            GuiObject guiObject = hideableObjects.get(i);
            guiObject.atTopLeft(hideableObjectsOffsetX, height);
            height -= guiObject.getHeight();
        }

        updateHeight();
    }

    protected void setCanMaximize(boolean canMaximize) {
        this.canMaximize = canMaximize;
        label.updatePositionAndSize();
    }

    @Override
    public MinimizableGuiObject setTextColor(float r, float g, float b, float a) {
        label.setColor(r, g, b, a);
        return this;
    }

    public MinimizableGuiObject setTextColor(Vector4f color) {
        setTextColor(color.x, color.y, color.z, color.w);
        return this;
    }

    public MinimizableGuiObject setName(String string) {
        label.setString(string);
        return this;
    }

    private int getStringOffsetX() {
        return canMaximize ? minimizableStringOffsetX : stringOffsetX;
    }

    public String getName() {
        return label.getString();
    }

    @Override
    public List<GuiObject> getGuiObjects() {
        return hideableObjects;
    }

    public List<GuiObject> getNonHideableObjects() {
        return guiObjects;
    }
}