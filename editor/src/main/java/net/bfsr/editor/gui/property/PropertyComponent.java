package net.bfsr.editor.gui.property;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.gui.object.AbstractGuiObject;
import net.bfsr.engine.gui.object.GuiObjectWithSubObjects;
import net.bfsr.engine.gui.object.GuiObjectsHandler;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class PropertyComponent extends GuiObjectWithSubObjects {
    @Getter
    protected final Object object;
    protected final Object[] values;
    protected final List<Field> fields;
    @Getter
    protected final StringObject stringObject;
    @Setter
    protected int propertyOffsetX;
    final int propertyYOffset;
    protected final FontType fontType;
    protected final int fontSize;
    int stringOffsetX;
    protected final int stringOffsetY;
    protected final BiConsumer<Object, Integer> valueConsumer;

    protected PropertyComponent(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX,
                                int propertyOffsetY, int stringOffsetY, Object object, List<Field> fields,
                                Object[] values, BiConsumer<Object, Integer> valueConsumer) {
        super(width, height);
        this.object = object;
        this.fields = fields;
        this.values = values;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.stringOffsetY = stringOffsetY;
        this.stringObject = new StringObject(fontType, name, fontSize).setColor(EditorTheme.TEXT_COLOR_GRAY,
                EditorTheme.TEXT_COLOR_GRAY, EditorTheme.TEXT_COLOR_GRAY, 1.0f).compile();
        this.propertyOffsetX = propertyOffsetX;
        this.propertyYOffset = propertyOffsetY;
        this.valueConsumer = valueConsumer;
        setHoverColor(0.3f, 0.3f, 0.3f, 0.5f);
    }

    protected PropertyComponent(AbstractGuiObject guiObject, int width, int height, String name, FontType fontType, int fontSize,
                                int propertyOffsetX, int propertyOffsetY, int stringOffsetY,
                                Object object, List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueConsumer) {
        this(width, height, name, fontType, fontSize, propertyOffsetX, propertyOffsetY, stringOffsetY, object, fields, values,
                valueConsumer);
        addSubObject(guiObject);
    }

    @Override
    public void update() {
        super.update();
        stringObject.update();
    }

    public void updatePositions() {
        Gui currentGui = Core.get().getGuiManager().getGui();
        if (currentGui instanceof GuiEditor) {
            ((GuiEditor<?, ?>) currentGui).updatePositions();
        }
    }

    @Override
    public void render() {
        if (isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }

    @Override
    protected void registerSubElements(GuiObjectsHandler gui) {
        super.registerSubElements(gui);
        gui.registerGuiObject(stringObject);
    }

    @Override
    protected void unregisterSubElements(GuiObjectsHandler gui) {
        super.unregisterSubElements(gui);
        gui.unregisterGuiObject(stringObject);
    }

    @Override
    public PropertyComponent atTopLeftCorner(int x, int y) {
        super.atTopLeftCorner(x, y);
        stringObject.atTopLeftCorner(x + stringOffsetX,
                y + stringOffsetY + fontType.getStringCache().getCenteredYOffset(stringObject.getString(), 20, fontSize));
        return this;
    }

    @Override
    public PropertyComponent atTopRightCorner(int x, int y) {
        super.atTopRightCorner(x, y);
        stringObject.atTopRightCorner(x + stringOffsetX,
                y + stringOffsetY + fontType.getStringCache().getCenteredYOffset(stringObject.getString(), 20, fontSize));
        return this;
    }

    @Override
    protected void setRepositionConsumerForSubObjects() {
        int offsetX = propertyOffsetX;
        for (int i = 0; i < subObjects.size(); i++) {
            AbstractGuiObject guiObject = subObjects.get(i);
            subObjectsRepositionConsumer.setup(guiObject, offsetX, propertyYOffset);
            offsetX += guiObject.getWidth();
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