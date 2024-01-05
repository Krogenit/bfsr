package net.bfsr.editor.gui.property;

import net.bfsr.client.Core;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.*;

public class PolygonProperty extends SimplePropertyList<Vector2fPropertiesHolder> {
    private final InputBox scaleInputBox;
    private final Button removeButton, polygonCreationModeButton, scaleButton;

    public PolygonProperty(int width, int height, String name, FontType fontType, int fontSize,
                           int propertyOffsetX, int stringOffsetY, Supplier<Vector2fPropertiesHolder> supplier, Object object,
                           List<Field> fields, Object[] values, PropertyGuiElementType propertyGuiElementType,
                           String propertyName, BiConsumer<Object, Integer> valueSetterConsumer) {
        super(width, height, name, fontType, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values,
                propertyGuiElementType, propertyName, valueSetterConsumer);
        removeButton = new Button(null, 0, 0, 20, 20, "", fontType, fontSize, stringOffsetY, () -> {
            if (properties.size() > 0) {
                remove(properties.get(properties.size() - 1));
                updatePositions();
            }
        }) {
            @Override
            public void render() {
                super.render();
                int centerX = x + width / 2;
                int centerY = y + height / 2;
                int offsetX = 1;
                int offsetY = 6;
                guiRenderer.addPrimitive(centerX - offsetY, centerY - offsetX, centerX - offsetY, centerY + offsetX,
                        centerX + offsetY, centerY + offsetX, centerX + offsetY, centerY - offsetX,
                        TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f, 0);
            }
        };
        setupButtonColors(removeButton);
        polygonCreationModeButton = new Button(null, 0, 0, 100, 20, "Edit polygon", fontType, fontSize, stringOffsetY, () -> {
            Gui gui = Core.get().getGuiManager().getGui();
            if (gui instanceof GuiEditor) {
                ((GuiEditor<?, ?>) gui).switchPolygonEditMode(this);
            }
        });
        setupButtonColors(polygonCreationModeButton);
        scaleInputBox = new InputBox(50, height, "", fontType, fontSize, 3, stringOffsetY);
        setupInputBoxColors(scaleInputBox);
        scaleButton = new Button(null, 0, 0, 60, 20, "Scale", fontType, fontSize, stringOffsetY, () -> {
            String string = scaleInputBox.getString();

            try {
                float scale = Float.parseFloat(string);

                for (int i = 0; i < objects.size(); i++) {
                    Vector2fPropertiesHolder holder = objects.get(i);
                    holder.setX(holder.getX() * scale);
                    holder.setY(holder.getY() * scale);

                    PropertyInputBox propertyComponent = ((PropertyInputBox) properties.get(i));
                    propertyComponent.inputBoxes.get(0).setString(holder.getX() + "");
                    propertyComponent.inputBoxes.get(1).setString(holder.getY() + "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        setupButtonColors(scaleButton);
        addConcealableObject(removeButton);
        addConcealableObject(polygonCreationModeButton);
        addConcealableObject(scaleInputBox);
        addConcealableObject(scaleButton);
    }

    @Override
    public PropertyComponent atTopRightCorner(int x, int y) {
        super.atTopRightCorner(x, y);

        int x1 = x + width - addButton.getWidth() - removeButton.getWidth();
        removeButton.atTopRightCorner(x1, y + height - baseHeight);
        x1 -= polygonCreationModeButton.getWidth();
        polygonCreationModeButton.atTopRightCorner(x1, y + height - baseHeight);
        x1 -= scaleButton.getWidth();
        scaleButton.atTopRightCorner(x1, y + height - baseHeight);
        x1 -= scaleInputBox.getWidth();
        scaleInputBox.atTopRightCorner(x1, y + height - baseHeight);
        return this;
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        removeButton.updatePositionAndSize(width, height);
        polygonCreationModeButton.updatePositionAndSize(width, height);
        scaleButton.updatePositionAndSize(width, height);
        scaleInputBox.updatePositionAndSize(width, height);
    }

    public List<Vector2fPropertiesHolder> getVertices() {
        return objects;
    }
}