package net.bfsr.editor.gui.property;

import net.bfsr.engine.gui.object.AbstractGuiObject;
import net.bfsr.engine.gui.object.GuiObjectsHandler;
import net.bfsr.engine.gui.object.SimpleGuiObject;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringCache;
import net.bfsr.engine.util.MutableInt;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static net.bfsr.editor.gui.component.MinimizableGuiObject.*;

public class MinimizablePropertyComponent extends PropertyComponent {
    private final List<AbstractGuiObject> concealableObjects = new ArrayList<>();
    boolean maximized;
    private final StringCache stringCache;
    int baseWidth;
    final int baseHeight;

    MinimizablePropertyComponent(int width, int height, String name, FontType fontType,
                                 int fontSize, int propertyOffsetX, int propertyOffsetY, int stringOffsetY,
                                 Object object,
                                 List<Field> fields, Object[] values,
                                 BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, fontType, fontSize, propertyOffsetX + MINIMIZABLE_STRING_X_OFFSET, propertyOffsetY + height,
                stringOffsetY, object, fields, values, valueConsumer);
        this.baseWidth = width;
        this.baseHeight = height;
        this.stringCache = fontType.getStringCache();
        this.stringOffsetX = MINIMIZABLE_STRING_X_OFFSET;
        setHeightResizeFunction((integer, integer2) -> this.height);
    }

    void addConcealableObject(AbstractGuiObject guiObject) {
        concealableObjects.add(guiObject);

        if (maximized && gui != null) {
            gui.registerGuiObject(guiObject);
        }
    }

    void removeConcealableObject(AbstractGuiObject guiObject) {
        concealableObjects.remove(guiObject);

        if (gui != null) {
            gui.unregisterGuiObject(guiObject);
        }
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

    private void maximize() {
        if (!maximized) {
            maximized = true;
            registerConcealableObjects();
            updatePositions();
        }
    }

    private void minimize() {
        if (maximized) {
            maximized = false;
            unregisterConcealableObjects();
            updatePositions();
        }
    }

    protected void registerConcealableObjects() {
        if (gui != null) {
            for (int i = 0; i < concealableObjects.size(); i++) {
                gui.registerGuiObject(concealableObjects.get(i));
            }
        }
    }

    protected void unregisterConcealableObjects() {
        if (gui != null) {
            for (int i = 0; i < concealableObjects.size(); i++) {
                gui.unregisterGuiObject(concealableObjects.get(i));
            }
        }
    }

    @Override
    protected void registerSubElements(GuiObjectsHandler gui) {
        super.registerSubElements(gui);

        if (maximized) {
            registerConcealableObjects();
        }
    }

    @Override
    protected void unregisterSubElements(GuiObjectsHandler gui) {
        super.unregisterSubElements(gui);
        unregisterConcealableObjects();
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
        super.render();

        float interpolation = renderer.getInterpolation();
        renderTriangle((int) (lastX + (x - lastX) * interpolation + 10),
                (int) (lastY + (y - lastY) * interpolation + 10));
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
        stringObject.setY(y + stringCache.getCenteredYOffset(stringObject.getString(), baseHeight, fontSize) + stringOffsetY);
        MutableInt subObjectsY = new MutableInt(y + height);
        forEachSubObject(guiObject -> guiObject.setY(subObjectsY.getAndAdd(guiObject.getHeight())));
    }

    @Override
    public MinimizablePropertyComponent setPosition(int x, int y) {
        super.setPosition(x, y);
        stringObject.setPosition(x + MINIMIZABLE_STRING_X_OFFSET,
                y + stringCache.getCenteredYOffset(stringObject.getString(), baseHeight, fontSize) + stringOffsetY);
        MutableInt subObjectsY = new MutableInt(y + height);
        forEachSubObject(guiObject -> guiObject.setPosition(x + MINIMIZABLE_STRING_X_OFFSET,
                subObjectsY.getAndAdd(guiObject.getHeight())));
        return this;
    }

    @Override
    public MinimizablePropertyComponent setTextColor(float r, float g, float b, float a) {
        stringObject.setColor(r, g, b, a).compile();
        return this;
    }

    public MinimizablePropertyComponent setName(String string) {
        stringObject.setString(string);
        return this;
    }

    @Override
    public SimpleGuiObject setWidth(int width) {
        this.baseWidth = width;
        return super.setWidth(width);
    }

    @Override
    public int getHeight() {
        return height;
    }

    public String getName() {
        return stringObject.getString();
    }

    @Override
    public void setSetting() throws IllegalAccessException {}
}