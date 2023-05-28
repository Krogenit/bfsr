package net.bfsr.editor.gui.property;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.editor.gui.ColorScheme;
import net.bfsr.editor.property.PropertiesHolder;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.gui.object.AbstractGuiObject;
import net.bfsr.engine.gui.object.GuiObjectWithSubObjects;
import net.bfsr.engine.gui.object.GuiObjectsHandler;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.List;

public abstract class PropertyComponent<P extends PropertiesHolder> extends GuiObjectWithSubObjects {
    protected final P object;
    protected final Object[] values;
    protected final List<Field> fields;
    @Getter
    private final StringObject stringObject;
    @Setter
    protected int propertyOffsetX;
    protected final int propertyYOffset;
    protected final FontType fontType;
    protected final int fontSize;
    protected final int stringOffsetY;

    protected PropertyComponent(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX,
                                int propertyOffsetY, int stringOffsetY, P object, List<Field> fields,
                                Object[] values) {
        super(width, height);
        this.object = object;
        this.fields = fields;
        this.values = values;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.stringOffsetY = stringOffsetY;
        this.stringObject =
                new StringObject(fontType, name, fontSize).setColor(ColorScheme.TEXT_COLOR_GRAY, ColorScheme.TEXT_COLOR_GRAY,
                        ColorScheme.TEXT_COLOR_GRAY, 1.0f).compile();
        this.propertyOffsetX = propertyOffsetX;
        this.propertyYOffset = propertyOffsetY;
        setHoverColor(0.3f, 0.3f, 0.3f, 0.5f);
    }

    protected PropertyComponent(AbstractGuiObject guiObject, int width, int height, String name, FontType fontType, int fontSize,
                                int propertyOffsetX, int propertyOffsetY, int stringOffsetY,
                                P object, List<Field> fields, Object[] values) {
        this(width, height, name, fontType, fontSize, propertyOffsetX, propertyOffsetY, stringOffsetY, object, fields, values);
        addSubObject(guiObject);
    }

    @Override
    public void render() {
        if (isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }

    @Override
    protected void registerSubElements(GuiObjectsHandler gui) {
        gui.registerGuiObject(stringObject);
        super.registerSubElements(gui);
    }

    @Override
    protected void unregisterSubElements(GuiObjectsHandler gui) {
        gui.unregisterGuiObject(stringObject);
        super.unregisterSubElements(gui);
    }

    @Override
    public PropertyComponent<P> atTopLeftCorner(int x, int y) {
        super.atTopLeftCorner(x, y);
        stringObject.atTopLeftCorner(x,
                y + stringOffsetY + fontType.getStringCache().getCenteredYOffset(stringObject.getString(), 20, fontSize));
        return this;
    }

    @Override
    public PropertyComponent<P> atTopRightCorner(int x, int y) {
        super.atTopRightCorner(x, y);
        stringObject.atTopRightCorner(x,
                y + stringOffsetY + fontType.getStringCache().getCenteredYOffset(stringObject.getString(), 20, fontSize));
        return this;
    }

    @Override
    protected void setRepositionConsumerForSubObjects() {
        int offsetX = propertyOffsetX;
        for (int i = 0; i < subObjects.size(); i++) {
            subObjectsRepositionConsumer.setup(subObjects.get(i), offsetX, propertyYOffset);
            offsetX += subObjects.get(i).getWidth();
        }
    }

    public abstract void setSetting() throws IllegalAccessException;

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        stringObject.updatePositionAndSize(width, height);
        forEachSubObject(guiObject -> guiObject.updatePositionAndSize(width, height));
    }
}