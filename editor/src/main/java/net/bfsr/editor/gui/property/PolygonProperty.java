package net.bfsr.editor.gui.property;

import lombok.extern.log4j.Log4j2;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.InputBox;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.setupButton;
import static net.bfsr.editor.gui.EditorTheme.setupInputBox;

@Log4j2
public class PolygonProperty extends SimplePropertyList<Vector2fPropertiesHolder> {
    public PolygonProperty(int width, int height, String name, String fontName, int fontSize, int propertyOffsetX, int stringOffsetY,
                           Supplier<Vector2fPropertiesHolder> supplier, Object object, List<Field> fields, Object[] values,
                           PropertyGuiElementType propertyGuiElementType, String propertyName, BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, fontName, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values,
                propertyGuiElementType, propertyName, valueConsumer);
        Button polygonCreationModeButton = new Button(0, 0, 100, 20, "Edit polygon", fontName, fontSize, stringOffsetY,
                (mouseX, mouseY) -> {
                    Gui gui = guiManager.getGui();
                    if (gui instanceof GuiEditor) {
                        ((GuiEditor<?, ?>) gui).switchPolygonEditMode(this);
                    }
                });
        int x1 = -40;
        add(setupButton(polygonCreationModeButton).atBottomRight(x1, 0));

        Button scaleButton = new Button(60, 20, "Scale", fontName, fontSize, stringOffsetY);
        x1 -= polygonCreationModeButton.getWidth();
        add(setupButton(scaleButton).atBottomRight(x1, 0));

        InputBox scaleInputBox = new InputBox(50, height, "", fontName, fontSize, 3, stringOffsetY);
        x1 -= scaleButton.getWidth();
        add(setupInputBox(scaleInputBox).atBottomRight(x1, 0));

        scaleButton.setLeftReleaseConsumer((mouseX, mouseY) -> {
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