package net.bfsr.editor.gui.property;

import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.component.MinimizableGuiObject;
import net.bfsr.editor.gui.component.receive.InputBoxPropertyReceiver;
import net.bfsr.editor.gui.component.receive.PropertyReceiver;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.converter.ConverterUtils;
import net.bfsr.editor.property.converter.PropertyConverter;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.object.SimpleGuiObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PropertyInputBox extends PropertyComponent {
    private static final int MAX_LINE_SIZE = 500;

    final List<InputBox> inputBoxes = new ArrayList<>();

    public PropertyInputBox(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY,
                            Object object, List<Field> fields, Object[] values, List<Class<?>> types,
                            BiConsumer<Object, Integer> valueSetterConsumer) {
        super(width, height, name, EditorTheme.FONT_TYPE, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values,
                valueSetterConsumer);
        this.propertyOffsetX = stringObject.getWidth() + MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET;
        int inputBoxWidth = (width - propertyOffsetX) / values.length;

        for (int i = 0; i < values.length; i++) {
            PropertyConverter converter = ConverterUtils.getConverter(types.get(i));
            String value = converter.toString(values[i]);
            Field field = fields.get(i);
            Property annotation = field.getAnnotation(Property.class);
            Class<? extends PropertyReceiver> receiveHandler = annotation.receiveHandler();
            try {
                InputBoxPropertyReceiver inputBox = new InputBoxPropertyReceiver(inputBoxWidth, height, "", EditorTheme.FONT_TYPE,
                        fontSize, 3, stringOffsetY, MAX_LINE_SIZE, receiveHandler.getConstructor().newInstance()) {
                    @Override
                    public boolean onMouseScroll(float y) {
                        if (!isMouseHover()) return false;

                        String string = getString();

                        try {
                            Double.parseDouble(string);

                            if (string.contains(".")) {
                                setString((Float.parseFloat(string) + y) + "");
                            } else {
                                setString((Integer.parseInt(string.isEmpty() ? "0" : string) + (int) y) + "");
                            }

                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                };
                inputBox.setOnUnselectedRunnable(this::setSetting);
                EditorTheme.setupInputBoxColors(inputBox);
                inputBox.setString(value == null ? "" : value);
                addSubObject(inputBox);
                inputBoxes.add(inputBox);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setSetting() {
        for (int i = 0; i < inputBoxes.size(); i++) {
            Class<?> type = fields.get(i).getType();
            PropertyConverter converter = ConverterUtils.getConverter(type);
            valueConsumer.accept(converter.fromString(type, inputBoxes.get(i).getString()), i);
        }
    }

    @Override
    public SimpleGuiObject setWidth(int width) {
        int inputBoxWidth = (width - propertyOffsetX) / subObjects.size();
        for (int i = 0; i < subObjects.size(); i++) {
            subObjects.get(i).setWidth(inputBoxWidth);
        }
        return super.setWidth(width);
    }
}