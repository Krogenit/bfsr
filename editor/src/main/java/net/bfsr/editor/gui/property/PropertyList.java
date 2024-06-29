package net.bfsr.editor.gui.property;

import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.TEXT_COLOR_GRAY;
import static net.bfsr.editor.gui.EditorTheme.setupButton;

abstract class PropertyList<T extends PropertyComponent, O> extends PropertyObject<T> {
    final Button addButton;
    final Supplier<O> supplier;
    final int contextMenuStringXOffset = 8;

    PropertyList(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX, int stringOffsetY,
                 Supplier<O> supplier, Object object, List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, fontType, fontSize, propertyOffsetX, stringOffsetY, object, fields, values, valueConsumer);
        this.supplier = supplier;
        int addButtonSize = 20;
        add(addButton = new Button(width - addButtonSize, height - baseHeight, addButtonSize, addButtonSize, "", fontType,
                fontSize, stringOffsetY, () -> addProperty(createObject())));
        setupButton(addButton).atBottomRight(-addButton.getWidth(), -addButton.getHeight());
        addButton.setRenderer(new GuiObjectRenderer(addButton) {
            @Override
            public void render(int lastX, int lastY, int x, int y, int width, int height) {
                if (guiObject.isMouseHover()) {
                    guiRenderer.add(lastX, lastY, x, y, width, height, outlineHoverColor);
                    guiRenderer.add(lastX + 1, lastY + 1, x + 1, y + 1, width - 2, height - 2, hoverColor);
                } else {
                    guiRenderer.add(lastX, lastY, x, y, width, height, outlineColor);
                    guiRenderer.add(lastX + 1, lastY + 1, x + 1, y + 1, width - 2, height - 2, color);
                }

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

                super.render(lastX, lastY, x, y, width, height);
            }
        });
    }

    @Override
    protected int getMaximizedHeight() {
        return super.getMaximizedHeight() + addButton.getHeight();
    }

    protected abstract O createObject();

    public abstract void addProperty(O propertiesHolder);

    protected void removeProperty(T guiObject) {
        properties.remove(guiObject);
        remove(guiObject);
    }
}