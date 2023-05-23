package net.bfsr.editor.gui.component;

import net.bfsr.client.Core;
import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiObjectsHandler;
import net.bfsr.client.gui.SimpleGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.util.RunnableUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.ColorScheme.*;

public abstract class PropertyArray<P extends PropertiesHolder, T extends AbstractGuiObject, O> extends PropertyComponent<P> {
    protected final List<T> propertiesHolders = new ArrayList<>();
    protected int baseWidth;
    protected final int baseHeight;
    protected final Button addButton;
    protected final Supplier<O> supplier;
    protected final int contextMenuStringXOffset = 8;

    protected PropertyArray(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX, int propertyOffsetY, int stringOffsetY, Supplier<O> supplier, P object,
                            List<Field> fields, Object[] values) {
        super(width, height, name, fontType, fontSize, propertyOffsetX, propertyOffsetY, stringOffsetY, object, fields, values);
        this.baseWidth = width;
        this.baseHeight = height;
        this.supplier = supplier;
        addButton = new Button(null, 0, 0, 20, 20, "", fontType, fontSize, stringOffsetY, RunnableUtils.EMPTY_RUNNABLE) {
            @Override
            public void render() {
                super.render();
                int centerX = x + width / 2;
                int centerY = y + height / 2;
                int offsetX = 1;
                int offsetY = 6;
                Engine.renderer.guiRenderer.addPrimitive(centerX - offsetX, centerY - offsetY, centerX - offsetX, centerY + offsetY, centerX + offsetX, centerY + offsetY, centerX + offsetX,
                        centerY - offsetY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f, 0);
                Engine.renderer.guiRenderer.addPrimitive(centerX - offsetY, centerY - offsetX, centerX - offsetY, centerY + offsetX, centerX + offsetY, centerY + offsetX, centerX + offsetY,
                        centerY - offsetX, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f, 0);
            }
        };
        addButton.setOnMouseClickRunnable(() -> {
            add(createObject());
            updatePositions();
        });
        addButton.setColor(BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, 1.0f);
        addButton.setHoverColor(BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, 1.0f);
        addButton.setOutlineColor(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f);
        addButton.setOutlineHoverColor(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f);
        addButton.setTextColor(TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f);
    }

    protected abstract O createObject();

    public abstract T add(O propertiesHolder);

    protected void remove(T guiObject) {
        removeSubObject(guiObject);
        gui.unregisterGuiObject(guiObject);
        propertiesHolders.remove(guiObject);
        updatePositions();
    }


    @Override
    protected void setRepositionConsumerForSubObjects() {
        int height = 0;
        for (int i = 0; i < propertiesHolders.size(); i++) {
            T guiObject = propertiesHolders.get(i);
            subObjectsRepositionConsumer.setup(guiObject, propertyOffsetX, height);
            updatePropertiesOffset(guiObject);
            height += guiObject.getHeight();
        }

        this.height = baseHeight + height;
    }

    protected abstract void updatePropertiesOffset(T guiObject);

    @Override
    public PropertyComponent<P> atTopRightCorner(int x, int y) {
        super.atTopRightCorner(x, y);
        addButton.atTopRightCorner(x + width - addButton.getWidth(), y + height - baseHeight);
        return this;
    }

    protected void updatePositions() {
        Gui currentGui = Core.get().getGuiManager().getCurrentGui();
        if (currentGui instanceof GuiEditor) {
            ((GuiEditor) currentGui).updatePositions();
        }
    }

    @Override
    protected void registerSubElements(GuiObjectsHandler gui) {
        super.registerSubElements(gui);
        gui.registerGuiObject(addButton);
    }

    @Override
    protected void unregisterSubElements(GuiObjectsHandler gui) {
        super.unregisterSubElements(gui);
        gui.unregisterGuiObject(addButton);
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        addButton.updatePositionAndSize(width, height);
    }

    @Override
    public SimpleGuiObject setWidth(int width) {
        this.baseWidth = width;
        return super.setWidth(width);
    }
}