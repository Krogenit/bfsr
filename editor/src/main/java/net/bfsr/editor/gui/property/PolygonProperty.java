package net.bfsr.editor.gui.property;

import lombok.extern.log4j.Log4j2;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.renderer.font.glyph.Font;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.setupButton;
import static net.bfsr.editor.gui.EditorTheme.setupInputBox;

@Log4j2
public class PolygonProperty extends SimplePropertyList<Vector2fPropertiesHolder> {
    public PolygonProperty(int width, int height, String name, Font font, int fontSize, int propertyOffsetX, int stringOffsetY,
                           Supplier<Vector2fPropertiesHolder> supplier, Object object, List<Field> fields, Object[] values,
                           PropertyGuiElementType propertyGuiElementType, String propertyName, BiConsumer<Object, Integer> valueConsumer,
                           Runnable changeValueListener) {
        super(width, height, name, font, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values,
                propertyGuiElementType, propertyName, valueConsumer, changeValueListener);
        Button polygonCreationModeButton = new Button(100, 20, "Edit polygon", font, fontSize, stringOffsetY,
                (mouseX, mouseY) -> {
                    Gui gui = guiManager.getGui();
                    if (gui instanceof GuiEditor) {
                        ((GuiEditor<?, ?>) gui).switchPolygonEditMode(this);
                    }
                });
        int x1 = -40;
        int y1 = 0;
        add(setupButton(polygonCreationModeButton).atBottomRight(x1, 0));
        y1 += 20;
        x1 = 0;

        Button scaleButton = new Button(60, 20, "Scale", font, fontSize, stringOffsetY);
        add(setupButton(scaleButton).atBottomRight(x1, y1));
        x1 -= scaleButton.getWidth();

        InputBox scaleYInputBox = new InputBox(50, height, "1.0", font, fontSize, 3, stringOffsetY);
        add(setupInputBox(scaleYInputBox).atBottomRight(x1, y1));
        x1 -= scaleYInputBox.getWidth();

        InputBox scaleXInputBox = new InputBox(50, height, "1.0", font, fontSize, 3, stringOffsetY);
        add(setupInputBox(scaleXInputBox).atBottomRight(x1, y1));

        scaleButton.setLeftReleaseConsumer((mouseX, mouseY) -> {
            String scaleXString = scaleXInputBox.getString();
            String scaleYString = scaleYInputBox.getString();

            try {
                float scaleX = Float.parseFloat(scaleXString);
                float scaleY = Float.parseFloat(scaleYString);
                modifyVertices(aFloat -> aFloat * scaleX, aFloat -> aFloat * scaleY);
            } catch (NumberFormatException e) {
                log.error("Failed to parse float for input strings {}, {}", scaleXString, scaleYString);
            }
        });

        y1 += 20;
        x1 = 0;

        Button moveButton = new Button(60, 20, "Move", font, fontSize, stringOffsetY);
        add(setupButton(moveButton).atBottomRight(x1, y1));
        x1 -= moveButton.getWidth();

        InputBox moveYInputBox = new InputBox(50, height, "0.0", font, fontSize, 3, stringOffsetY);
        add(setupInputBox(moveYInputBox).atBottomRight(x1, y1));
        x1 -= moveYInputBox.getWidth();

        InputBox moveXInputBox = new InputBox(50, height, "0.0", font, fontSize, 3, stringOffsetY);
        add(setupInputBox(moveXInputBox).atBottomRight(x1, y1));

        moveButton.setLeftReleaseConsumer((mouseX, mouseY) -> {
            String moveXString = moveXInputBox.getString();
            String moveYString = moveYInputBox.getString();

            try {
                float moveX = Float.parseFloat(moveXString);
                float moveY = Float.parseFloat(moveYString);
                modifyVertices(aFloat -> aFloat + moveX, aFloat -> aFloat + moveY);
            } catch (NumberFormatException e) {
                log.error("Failed to parse float for input strings {}, {}", moveXString, moveYString);
            }
        });
    }

    private void modifyVertices(Function<Float, Float> xFunction, Function<Float, Float> yFunction) {
        for (int i = 0; i < objects.size(); i++) {
            Vector2fPropertiesHolder holder = objects.get(i);
            holder.setX(xFunction.apply(holder.getX()));
            holder.setY(yFunction.apply(holder.getY()));
            setInputBoxesValue(i, holder.getX(), holder.getY());
        }
    }

    public void setInputBoxesValue(int index, float x, float y) {
        PropertyInputBox propertyComponent = ((PropertyInputBox) properties.get(index));
        propertyComponent.inputBoxes.get(0).setString(x + "");
        propertyComponent.inputBoxes.get(1).setString(y + "");
    }

    public List<Vector2fPropertiesHolder> getVertices() {
        return objects;
    }

    @Override
    protected int getMaximizedHeight() {
        return super.getMaximizedHeight() + 40;
    }
}