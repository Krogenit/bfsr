package net.bfsr.editor.gui.property;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Core;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.renderer.RectangleOutlinedRenderer;
import net.bfsr.engine.renderer.font.Font;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.TEXT_COLOR_GRAY;
import static net.bfsr.editor.gui.EditorTheme.setupButton;
import static net.bfsr.editor.gui.EditorTheme.setupInputBox;

@Log4j2
public class PolygonProperty extends SimplePropertyList<Vector2fPropertiesHolder> {
    public PolygonProperty(int width, int height, String name, Font font, int fontSize, int propertyOffsetX, int stringOffsetY,
                           Supplier<Vector2fPropertiesHolder> supplier, Object object, List<Field> fields, Object[] values,
                           PropertyGuiElementType propertyGuiElementType, String propertyName, BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, font, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values,
                propertyGuiElementType, propertyName, valueConsumer);

        int x1 = -addButton.getWidth() - 20;
        Button removeButton = new Button(0, 0, 20, 20, "", font, fontSize, stringOffsetY, () -> {
            if (properties.size() > 0) {
                removeProperty(properties.get(properties.size() - 1));
            }
        });
        add(setupButton(removeButton).atBottomRight(x1, -baseHeight));
        removeButton.setRenderer(new RectangleOutlinedRenderer(removeButton) {
            @Override
            public void render(int lastX, int lastY, int x, int y, int width, int height) {
                super.render(lastX, lastY, x, y, width, height);

                int centerX = x + width / 2;
                int centerY = y + height / 2;
                int offsetX = 1;
                int offsetY = 6;
                guiRenderer.addPrimitive(centerX - offsetY, centerY - offsetX, centerX - offsetY, centerY + offsetX,
                        centerX + offsetY, centerY + offsetX, centerX + offsetY, centerY - offsetX,
                        TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f, 0);
            }
        });

        Button polygonCreationModeButton = new Button(0, 0, 100, 20, "Edit polygon", font, fontSize, stringOffsetY, () -> {
            Gui gui = Core.get().getGuiManager().getGui();
            if (gui instanceof GuiEditor) {
                ((GuiEditor<?, ?>) gui).switchPolygonEditMode(this);
            }
        });
        x1 -= polygonCreationModeButton.getWidth();
        add(setupButton(polygonCreationModeButton).atBottomRight(x1, -baseHeight));

        Button scaleButton = new Button(60, 20, "Scale", font, fontSize, stringOffsetY);
        x1 -= scaleButton.getWidth();
        add(setupButton(scaleButton).atBottomRight(x1, -baseHeight));

        InputBox scaleInputBox = new InputBox(50, height, "", font, fontSize, 3, stringOffsetY);
        x1 -= scaleInputBox.getWidth();
        add(setupInputBox(scaleInputBox).atBottomRight(x1, -baseHeight));

        scaleButton.setLeftReleaseRunnable(() -> {
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
            } catch (NumberFormatException e) {
                log.error("Failed to parse float for input string {}", string);
            }
        });
    }

    public List<Vector2fPropertiesHolder> getVertices() {
        return objects;
    }
}