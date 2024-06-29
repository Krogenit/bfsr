package net.bfsr.editor.gui.component;

import lombok.Getter;
import net.bfsr.editor.gui.renderer.MinimizableGuiObjectRenderer;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringCache;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class MinimizableGuiObject extends GuiObject {
    public static final int MINIMIZABLE_STRING_X_OFFSET = 20;
    private static final int STATIC_STRING_X_OFFSET = 6;

    @Getter
    protected boolean maximized;
    @Getter
    protected final Label label;
    protected final FontType fontType;
    protected final int fontSize;
    private final StringCache stringCache;
    private final int stringOffsetX, minimizableStringOffsetX;
    protected final int stringOffsetY;
    @Getter
    protected boolean canMaximize = true;
    protected final List<GuiObject> hideableObjects = new ArrayList<>();
    @Getter
    protected final int baseHeight;

    public MinimizableGuiObject(int width, int height, String name, FontType fontType, int fontSize, int stringOffsetX, int stringOffsetY,
                                int minimizableStringOffsetX) {
        super(width, height);
        this.baseHeight = height;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.stringCache = fontType.getStringCache();
        this.label = new Label(fontType, name, fontSize).compileAtOrigin();
        addNonConcealable(
                label.atTopLeft(minimizableStringOffsetX, stringCache.getCenteredYOffset(name, height, fontSize) + stringOffsetY));
        this.stringOffsetX = stringOffsetX;
        this.stringOffsetY = stringOffsetY;
        this.minimizableStringOffsetX = minimizableStringOffsetX;
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

    public MinimizableGuiObject(int width, int height, String name, FontType fontType, int fontSize, int stringOffsetY) {
        this(width, height, name, fontType, fontSize, STATIC_STRING_X_OFFSET, stringOffsetY, MINIMIZABLE_STRING_X_OFFSET);
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
    }

    protected void minimize() {
        updateHeight();
        removeHideable();
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
        label.setStringAndCompileAtOrigin(name);
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
    }

    @Override
    public void addAt(int index, GuiObject guiObject) {
        addHideableAt(index, guiObject);
    }

    @Override
    public int addBefore(GuiObject guiObject, GuiObject beforeObject) {
        return addHideableBefore(guiObject, beforeObject);
    }

    @Override
    public void remove(GuiObject guiObject) {
        removeHideable(guiObject);
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        updateConcealableObjectsPositions();
    }

    public void updateConcealableObjectsPositions() {
        int height = baseHeight;
        for (int i = 0; i < hideableObjects.size(); i++) {
            GuiObject guiObject = hideableObjects.get(i);
            guiObject.atTopLeft(getStringOffsetX(), height);
            height += guiObject.getHeight();
        }

        updateHeight();
    }

    protected void setCanMaximize(boolean canMaximize) {
        this.canMaximize = canMaximize;
        label.atTopLeft(getStringOffsetX(),
                stringCache.getCenteredYOffset(label.getString(), baseHeight, fontSize) + stringOffsetY);
        label.updatePositionAndSize();
    }

    @Override
    public MinimizableGuiObject setTextColor(float r, float g, float b, float a) {
        label.setColor(r, g, b, a).compileAtOrigin();
        return this;
    }

    public MinimizableGuiObject setTextColor(Vector4f color) {
        setTextColor(color.x, color.y, color.z, color.w);
        return this;
    }

    public MinimizableGuiObject setName(String string) {
        label.setStringAndCompileAtOrigin(string);
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