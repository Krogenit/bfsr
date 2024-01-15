package net.bfsr.editor.gui.component;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.gui.object.AbstractGuiObject;
import net.bfsr.engine.gui.object.GuiObjectWithSubObjects;
import net.bfsr.engine.gui.object.GuiObjectsHandler;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringCache;
import net.bfsr.engine.util.MutableInt;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector4f;

public class MinimizableGuiObject extends GuiObjectWithSubObjects {
    public static final int MINIMIZABLE_STRING_X_OFFSET = 20;
    private static final int STATIC_STRING_X_OFFSET = 6;
    public static final int TRIANGLE_HALF_WIDTH = 4;
    public static final int TRIANGLE_HALF_HEIGHT = 4;

    @Getter
    protected boolean maximized;
    @Getter
    protected final StringObject stringObject;
    protected final int fontSize;
    private final StringCache stringCache;
    protected final int stringYOffset;
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
        this.stringObject = new StringObject(fontType, name, fontSize).compileAtOrigin();
        this.stringYOffset = stringYOffset;
        setHeightResizeFunction((integer, integer2) -> this.height);
    }

    @Override
    public void update() {
        super.update();
        stringObject.update();
    }

    @Override
    public boolean onMouseLeftRelease() {
        if (!isMouseHover()) return false;

        if (maximized) {
            minimize();
        } else {
            maximize();
        }

        return true;
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
        stringObject.setStringAndCompile(name);
    }

    protected void onStartMoving() {}

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
                x + (canMaximize ? MINIMIZABLE_STRING_X_OFFSET : STATIC_STRING_X_OFFSET),
                y + stringCache.getCenteredYOffset(stringObject.getString(), height, fontSize) + stringYOffset
        );
        return this;
    }

    @Override
    public AbstractGuiObject atTopRightCorner(int x, int y) {
        super.atTopRightCorner(x, y);
        stringObject.atTopRightCorner(
                x + (canMaximize ? MINIMIZABLE_STRING_X_OFFSET : STATIC_STRING_X_OFFSET),
                y + stringCache.getCenteredYOffset(stringObject.getString(), height, fontSize) + stringYOffset
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
    public void addSubObject(AbstractGuiObject object) {
        subObjects.add(object);
        if (gui != null && maximized) {
            gui.registerGuiObject(object);
        }
    }

    @Override
    public void addSubObject(int index, AbstractGuiObject object) {
        subObjects.add(index, object);
        if (gui != null && maximized) {
            gui.registerGuiObject(object);
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
            float interpolation = renderer.getInterpolation();
            renderTriangle((int) (lastX + (x - lastX) * interpolation + 10),
                    (int) (lastY + (y - lastY) * interpolation + height / 2));
        }

        stringObject.render();
    }

    protected void renderBase() {
        if (isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z,
                    hoverColor.w);
        }
    }

    private void renderTriangle(int centerX, int centerY) {
        Vector4f textColor = stringObject.getColor();

        if (maximized) {
            guiRenderer.addPrimitive(centerX - TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT, centerX,
                    centerY + TRIANGLE_HALF_HEIGHT, centerX + TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT,
                    centerX - TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT,
                    textColor.x, textColor.y, textColor.z, textColor.w, 0);
        } else {
            guiRenderer.addPrimitive(centerX - TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT,
                    centerX - TRIANGLE_HALF_WIDTH, centerY + TRIANGLE_HALF_HEIGHT, centerX + TRIANGLE_HALF_WIDTH, centerY,
                    centerX - TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT,
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
        stringObject.setPosition(x + (canMaximize ? MINIMIZABLE_STRING_X_OFFSET : STATIC_STRING_X_OFFSET),
                y + stringCache.getCenteredYOffset(stringObject.getString(), height, fontSize) + stringYOffset);
        MutableInt subObjectsY = new MutableInt(y + height);
        forEachSubObject(guiObject -> guiObject.setPosition(x + MINIMIZABLE_STRING_X_OFFSET,
                subObjectsY.getAndAdd(guiObject.getHeight())));
        return this;
    }

    @Override
    public MinimizableGuiObject setTextColor(float r, float g, float b, float a) {
        stringObject.setColor(r, g, b, a).compileAtOrigin();
        return this;
    }

    public MinimizableGuiObject setTextColor(Vector4f color) {
        this.setTextColor(color.x, color.y, color.z, color.w);
        return this;
    }

    public MinimizableGuiObject setName(String string) {
        stringObject.setStringAndCompile(string);
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