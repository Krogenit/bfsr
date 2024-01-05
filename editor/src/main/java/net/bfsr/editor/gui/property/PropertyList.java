package net.bfsr.editor.gui.property;

import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.*;

abstract class PropertyList<T extends PropertyComponent, O> extends PropertyObject<T> {
    final Button addButton;
    final Supplier<O> supplier;
    final int contextMenuStringXOffset = 8;

    PropertyList(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX,
                 int stringOffsetY, Supplier<O> supplier, Object object, List<Field> fields, Object[] values,
                 BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, fontType, fontSize, propertyOffsetX, stringOffsetY, object, fields, values,
                valueConsumer);
        this.supplier = supplier;
        addButton = new Button(null, 0, 0, 20, 20, "", fontType, fontSize, stringOffsetY, () -> {
            add(createObject());
            updatePositions();
        }) {
            @Override
            public void render() {
                super.render();
                int centerX = x + width / 2;
                int centerY = y + height / 2;
                int offsetX = 1;
                int offsetY = 6;
                guiRenderer.addPrimitive(centerX - offsetX, centerY - offsetY, centerX - offsetX, centerY + offsetY,
                        centerX + offsetX, centerY + offsetY, centerX + offsetX, centerY - offsetY,
                        TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f, 0);
                guiRenderer.addPrimitive(centerX - offsetY, centerY - offsetX, centerX - offsetY, centerY + offsetX,
                        centerX + offsetY, centerY + offsetX, centerX + offsetY, centerY - offsetX,
                        TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f, 0);
            }
        };
        addButton.setColor(BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, 1.0f);
        addButton.setHoverColor(BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, 1.0f);
        addButton.setOutlineColor(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f);
        addButton.setOutlineHoverColor(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f);
        addButton.setTextColor(TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f);
    }

    protected abstract O createObject();

    public abstract void add(O propertiesHolder);

    protected void remove(T guiObject) {
        properties.remove(guiObject);
        removeConcealableObject(guiObject);
        updatePositions();
    }

    @Override
    protected void setRepositionConsumerForSubObjects() {
        super.setRepositionConsumerForSubObjects();

        if (maximized) {
            height += addButton.getHeight();
        }
    }

    @Override
    public PropertyComponent atTopRightCorner(int x, int y) {
        super.atTopRightCorner(x, y);
        addButton.atTopRightCorner(x + width - addButton.getWidth(), y + height - baseHeight);
        return this;
    }

    @Override
    protected void registerConcealableObjects() {
        super.registerConcealableObjects();
        if (gui != null) {
            gui.registerGuiObject(addButton);
        }
    }

    @Override
    protected void unregisterConcealableObjects() {
        super.unregisterConcealableObjects();
        if (gui != null) {
            gui.unregisterGuiObject(addButton);
        }
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        addButton.updatePositionAndSize(width, height);
    }
}