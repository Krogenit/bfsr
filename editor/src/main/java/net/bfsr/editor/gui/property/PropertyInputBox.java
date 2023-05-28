package net.bfsr.editor.gui.property;

import net.bfsr.editor.gui.ColorScheme;
import net.bfsr.editor.gui.component.ComponentHolder;
import net.bfsr.editor.gui.component.receive.InputBoxPropertyReceiver;
import net.bfsr.editor.gui.component.receive.PropertyReceiver;
import net.bfsr.editor.property.PropertiesHolder;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.converter.ConverterUtils;
import net.bfsr.editor.property.converter.PropertyConverter;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.object.SimpleGuiObject;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PropertyInputBox<P extends PropertiesHolder> extends PropertyComponent<P> {
    protected final List<InputBox> inputBoxes = new ArrayList<>();
    private static final int MAX_LINE_SIZE = 500;

    public PropertyInputBox(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY, P object,
                            List<Field> fields, Object[] values,
                            Consumer<String> unselectedConsumer, List<Class<?>> types) {
        super(width, height, name, FontType.CONSOLA, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values);
        int inputBoxWidth = (width - propertyOffsetX) / values.length;

        for (int i = 0; i < values.length; i++) {
            PropertyConverter converter = ConverterUtils.getConverter(types.get(i));
            String value = converter.toString(values[i]);
            Field field = fields.get(i);
            Property annotation = field.getAnnotation(Property.class);
            Class<? extends PropertyReceiver> receiveHandler = annotation.receiveHandler();
            try {
                PropertyReceiver propertyReceiver = receiveHandler.getConstructor().newInstance();

                InputBoxPropertyReceiver inputBox =
                        new InputBoxPropertyReceiver(inputBoxWidth, height, "", FontType.CONSOLA, fontSize, 3, stringOffsetY,
                                MAX_LINE_SIZE) {
                            @Override
                            public boolean onMouseScroll(float y) {
                                if (!isMouseHover()) return false;

                                String string = getString();
                                if (string.contains(".")) {
                                    setString((Float.parseFloat(string.isEmpty() ? "0" : string) + y) + "");
                                } else {
                                    setString((Integer.parseInt(string.isEmpty() ? "0" : string) + (int) y) + "");
                                }

                                return true;
                            }

                            @Override
                            public boolean canAcceptDraggable(ComponentHolder<? extends PropertiesHolder> componentHolder) {
                                return propertyReceiver.canInsert(componentHolder);
                            }

                            @Override
                            public void acceptDraggable(ComponentHolder<? extends PropertiesHolder> componentHolder) {
                                setString(propertyReceiver.getValueForInputBox(componentHolder));
                            }
                        };
                inputBox.setOnUnselectedRunnable(() -> unselectedConsumer.accept(inputBox.getString()));
                ColorScheme.setupInputBoxColors(inputBox);
                inputBox.setString(value == null ? "" : value);
                addSubObject(inputBox);
                inputBoxes.add(inputBox);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
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