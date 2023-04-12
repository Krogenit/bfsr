package net.bfsr.editor.gui.component;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.gui.GuiObjectWithSubObjects;
import net.bfsr.client.gui.GuiObjectsHandler;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringCache;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import net.bfsr.util.MutableInt;
import net.bfsr.util.RunnableUtils;
import org.joml.Vector4f;

public class MinimizableGuiObject extends GuiObjectWithSubObjects {
    public static final int MINIMIZABLE_STRING_X_OFFSET = 20;
    public static final int STATIC_STRING_X_OFFSET = 6;

    @Getter
    protected boolean maximized;
    @Getter
    protected final StringObject stringObject;
    protected final int fontSize;
    private final StringCache stringCache;
    protected final int stringYOffset;
    private final int triangleHalfWidth = 4;
    private final int triangleHalfHeight = 4;
    @Setter
    private Runnable onMaximizeRunnable = RunnableUtils.EMPTY_RUNNABLE;
    @Setter
    private Runnable onMinimizeRunnable = RunnableUtils.EMPTY_RUNNABLE;
    @Setter
    @Getter
    protected boolean canMaximize = true;

    public MinimizableGuiObject(int width, int height, String name, FontType fontType, int fontSize, int stringYOffset) {
        super(width, height);
        this.fontSize = fontSize;
        this.stringCache = fontType.getStringCache();
        this.stringObject = new StringObject(fontType, name, fontSize).compile();
        this.stringYOffset = stringYOffset;
        setHeightResizeFunction((integer, integer2) -> this.height);
    }

    @Override
    public void update() {
        super.update();
        stringObject.update();
    }

    @Override
    public void onMouseLeftRelease() {
        if (!isMouseHover()) return;

        if (maximized) {
            minimize();
        } else {
            maximize();
        }
    }

    public void maximize() {
        if (canMaximize && !maximized) {
            maximized = true;
            registerSubElements(gui);
            onMaximizeRunnable.run();
        }
    }

    public void minimize() {
        if (maximized) {
            maximized = false;
            unregisterSubElements(gui);
            onMinimizeRunnable.run();
        }
    }

    protected void onNameChanged(String name) {
        stringObject.setString(name);
    }

    protected void onStartMoving() {}

    protected void onMoved() {}

    @Override
    protected void registerSubElements(GuiObjectsHandler gui) {
        if (maximized) {
            super.registerSubElements(gui);
        }
    }

    @Override
    public AbstractGuiObject atTopLeftCorner(int x, int y) {
        super.atTopLeftCorner(x, y);
        stringObject.atTopLeftCorner(
                x + (canMaximize ? MINIMIZABLE_STRING_X_OFFSET : STATIC_STRING_X_OFFSET), y + stringCache.getCenteredYOffset(stringObject.getString(), height, fontSize) + stringYOffset
        );
        return this;
    }

    @Override
    public AbstractGuiObject atTopRightCorner(int x, int y) {
        super.atTopRightCorner(x, y);
        stringObject.atTopRightCorner(
                x + (canMaximize ? MINIMIZABLE_STRING_X_OFFSET : STATIC_STRING_X_OFFSET), y + stringCache.getCenteredYOffset(stringObject.getString(), height, fontSize) + stringYOffset
        );
        return this;
    }

    @Override
    protected void setRepositionConsumerForSubObjects() {
        int height = this.height;
        for (int i = 0; i < subObjects.size(); i++) {
            AbstractGuiObject guiObject = subObjects.get(i);
            subObjectsRepositionConsumer.setup(guiObject, MINIMIZABLE_STRING_X_OFFSET, height);
            height += guiObject.getHeight();
        }
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        stringObject.updatePositionAndSize(width, height);
        forEachSubObject(guiObject -> guiObject.updatePositionAndSize(width, height));
    }

    @Override
    public void addSubObject(AbstractGuiObject guiObject) {
        subObjects.add(guiObject);
        if (gui != null && maximized) {
            gui.registerGuiObject(guiObject);
        }
    }

    @Override
    public void renderNoInterpolation() {
        super.renderNoInterpolation();
        renderTriangle(x + 10, y + height / 2);
    }

    @Override
    public void render() {
        renderBase();

        if (canMaximize) {
            float interpolation = Core.get().getRenderer().getInterpolation();
            renderTriangle(lastX + (x - lastX) * interpolation + 10, lastY + (y - lastY) * interpolation + height / 2);
        }

        stringObject.render();
    }

    protected void renderBase() {
        if (isMouseHover()) {
            GUIRenderer.get().add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }

    private void renderTriangle(float centerX, float centerY) {
        Vector4f textColor = stringObject.getColor();

        if (maximized) {
            GUIRenderer.get().addPrimitive(centerX - triangleHalfWidth, centerY - triangleHalfHeight, centerX, centerY + triangleHalfHeight, centerX + triangleHalfWidth, centerY - triangleHalfHeight, centerX - triangleHalfWidth, centerY - triangleHalfHeight,
                    textColor.x, textColor.y, textColor.z, textColor.w, 0);
        } else {
            GUIRenderer.get().addPrimitive(centerX - triangleHalfWidth, centerY - triangleHalfHeight, centerX - triangleHalfWidth, centerY + triangleHalfHeight, centerX + triangleHalfWidth, centerY, centerX - triangleHalfWidth, centerY - triangleHalfHeight,
                    textColor.x, textColor.y, textColor.z, textColor.w, 0);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        stringObject.setY(y + stringCache.getCenteredYOffset(stringObject.getString(), height, fontSize) + stringYOffset);
        MutableInt subObjectsY = new MutableInt(y + height);
        forEachSubObject(guiObject -> guiObject.setY(subObjectsY.getAndAdd(guiObject.getHeight())));
    }

    @Override
    public MinimizableGuiObject setPosition(int x, int y) {
        super.setPosition(x, y);
        stringObject.setPosition(x + (canMaximize ? MINIMIZABLE_STRING_X_OFFSET : STATIC_STRING_X_OFFSET), y + stringCache.getCenteredYOffset(stringObject.getString(), height, fontSize) + stringYOffset);
        MutableInt subObjectsY = new MutableInt(y + height);
        forEachSubObject(guiObject -> guiObject.setPosition(x + MINIMIZABLE_STRING_X_OFFSET, subObjectsY.getAndAdd(guiObject.getHeight())));
        return this;
    }

    public MinimizableGuiObject setTextColor(float r, float g, float b, float a) {
        stringObject.setColor(r, g, b, a).compile();
        return this;
    }

    public MinimizableGuiObject setTextColor(Vector4f color) {
        this.setTextColor(color.x, color.y, color.z, color.w);
        return this;
    }

    public MinimizableGuiObject setName(String string) {
        stringObject.setString(string);
        return this;
    }

    @Override
    public int getHeight() {
        if (maximized) {
            int height = this.height;
            for (int i = 0; i < subObjects.size(); i++) {
                height += subObjects.get(i).getHeight();
            }
            return height;
        } else {
            return height;
        }
    }

    public String getName() {
        return stringObject.getString();
    }
}