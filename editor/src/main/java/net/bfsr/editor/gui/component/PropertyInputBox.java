package net.bfsr.editor.gui.component;

import net.bfsr.client.gui.SimpleGuiObject;
import net.bfsr.client.gui.input.InputBox;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.editor.property.ConverterUtils;
import net.bfsr.editor.property.PropertyConverter;
import net.bfsr.property.PropertiesHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.bfsr.editor.gui.ColorScheme.*;

public class PropertyInputBox<P extends PropertiesHolder> extends PropertyComponent<P> {
    protected final List<InputBox> inputBoxes = new ArrayList<>();
    private static final int MAX_LINE_SIZE = 500;

    public PropertyInputBox(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY, P object, List<Field> fields, Object[] values,
                            Consumer<String> unselectedConsumer, List<Class<?>> types) {
        super(width, height, name, FontType.CONSOLA, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values);
        int inputBoxWidth = (width - propertyOffsetX) / values.length;

        for (int i = 0; i < values.length; i++) {
            PropertyConverter converter = ConverterUtils.getConverter(types.get(i));
            String value = converter.toString(values[i]);

            InputBox inputBox = new InputBox(inputBoxWidth, height, "", FontType.CONSOLA, fontSize, 3, stringOffsetY, MAX_LINE_SIZE) {
                @Override
                public void onMouseScroll(float y) {
                    if (isMouseHover()) {
                        String string = getString();
                        if (string.contains(".")) {
                            setString((Float.parseFloat(string.isEmpty() ? "0" : string) + y) + "");
                        } else {
                            setString((Integer.parseInt(string.isEmpty() ? "0" : string) + (int) y) + "");
                        }
                    }
                }
            };
            inputBox.setOnUnselectedRunnable(() -> unselectedConsumer.accept(inputBox.getString()));
            inputBox.setColor(INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, 1.0f);
            inputBox.setHoverColor(INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, 1.0f);
            inputBox.setOutlineColor(INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, 1.0f);
            inputBox.setOutlineHoverColor(INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY, 1.0f);
            inputBox.setTextColor(TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f);
            inputBox.setString(value == null ? "" : value);
            addSubObject(inputBox);
            inputBoxes.add(inputBox);
        }
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            field.set(object, ConverterUtils.getConverter(field.getType()).fromString(inputBoxes.get(i).getString()));
        }
    }

    @Override
    public SimpleGuiObject setWidth(int width) {
        int inputBoxWidth = (width - propertyOffsetX) / values.length;
        for (int i = 0; i < subObjects.size(); i++) {
            subObjects.get(i).setWidth(inputBoxWidth);
        }
        return super.setWidth(width);
    }
}