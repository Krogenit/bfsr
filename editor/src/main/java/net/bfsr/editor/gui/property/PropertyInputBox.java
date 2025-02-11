package net.bfsr.editor.gui.property;

import lombok.extern.log4j.Log4j2;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.component.receive.InputBoxPropertyReceiver;
import net.bfsr.editor.gui.component.receive.PropertyReceiver;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.converter.ConverterUtils;
import net.bfsr.editor.property.converter.PropertyConverter;
import net.bfsr.engine.gui.component.InputBox;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Log4j2
public class PropertyInputBox extends PropertyComponent {
    private static final int MAX_LINE_SIZE = 500;

    final List<InputBox> inputBoxes = new ArrayList<>();

    public PropertyInputBox(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY, Object object,
                            List<Field> fields, Object[] values, List<Class<?>> types, BiConsumer<Object, Integer> valueConsumer,
                            Runnable changeValueListener) {
        super(width, height, name, EditorTheme.FONT, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values, valueConsumer,
                changeValueListener);
        this.propertyOffsetX = label.getWidth() + MINIMIZABLE_STRING_X_OFFSET;
        int inputBoxWidth = (width - propertyOffsetX) / values.length;
        int offsetX = propertyOffsetX;

        for (int i = 0; i < values.length; i++) {
            PropertyConverter converter = ConverterUtils.getConverter(types.get(i));
            String value = converter.toString(values[i]);
            Field field = fields.get(i);
            Property annotation = field.getAnnotation(Property.class);
            Class<? extends PropertyReceiver> receiveHandler = annotation.receiveHandler();
            try {
                InputBoxPropertyReceiver inputBox = new InputBoxPropertyReceiver(inputBoxWidth, height, "",
                        EditorTheme.FONT, fontSize, 3, stringOffsetY, MAX_LINE_SIZE, receiveHandler.getConstructor().newInstance()) {
                    @Override
                    public boolean mouseScroll(int mouseX, int mouseY, float scrollY) {
                        boolean mouseScroll = super.mouseScroll(mouseX, mouseY, scrollY);

                        if (!mouseScroll && typing) {
                            String string = getString();

                            try {
                                Double.parseDouble(string);

                                if (string.contains(".")) {
                                    setString((Float.parseFloat(string) + scrollY) + "");
                                } else {
                                    setString((Integer.parseInt(string.isEmpty() ? "0" : string) + (int) scrollY) + "");
                                }
                            } catch (NumberFormatException e) {
                                log.error("Error changing value in input box", e);
                            }

                            return true;
                        }

                        return false;
                    }
                };
                inputBox.setOnUnselectedRunnable(this::setSetting);
                EditorTheme.setupInputBox(inputBox);
                inputBox.setString(value == null ? "" : value);
                addNonConcealable(inputBox.atTopLeft(offsetX, propertyOffsetY));
                inputBoxes.add(inputBox);
                offsetX += inputBox.getWidth();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void setSetting() {
        for (int i = 0; i < inputBoxes.size(); i++) {
            Class<?> type = fields.get(i).getType();
            PropertyConverter converter = ConverterUtils.getConverter(type);
            valueConsumer.accept(converter.fromString(type, inputBoxes.get(i).getString()), i);
        }
    }

    @Override
    public void updateConcealableObjectsPositions() {
        int inputBoxWidth = (width - propertyOffsetX) / inputBoxes.size();
        int addWidthForLastInputBox = width - propertyOffsetX - inputBoxWidth * inputBoxes.size();
        int offsetX = propertyOffsetX;
        for (int i = 0; i < inputBoxes.size(); i++) {
            InputBox inputBox = inputBoxes.get(i);
            inputBox.atTopLeft(offsetX, propertyOffsetY);
            inputBox.setWidth(inputBoxWidth + (i == inputBoxes.size() - 1 ? addWidthForLastInputBox : 0));
            offsetX += inputBox.getWidth();
        }
    }
}