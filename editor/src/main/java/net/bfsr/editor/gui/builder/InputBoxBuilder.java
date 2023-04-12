package net.bfsr.editor.gui.builder;

import net.bfsr.client.renderer.font.FontType;
import net.bfsr.editor.gui.component.PropertyComponent;
import net.bfsr.editor.gui.component.PropertyInputBox;
import net.bfsr.editor.property.ConverterUtils;
import net.bfsr.property.PropertiesHolder;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InputBoxBuilder extends ComponentBuilder {
    @Override
    public <V extends PropertiesHolder> PropertyComponent<V> build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize, int stringOffsetY,
                                                                   List<Field> fields, Object[] values, V object) {
        return new PropertyInputBox<>(width, height, propertyName, offsetX, fontSize, stringOffsetY, object, fields, values, s -> {},
                fields.stream().map(Field::getType).collect(Collectors.toList()));
    }

    @Override
    public <V extends PropertiesHolder, PRIMITIVE_TYPE> PropertyComponent<V> build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize, int stringOffsetY,
                                                                                   List<Field> fields, Object[] values, Consumer<PRIMITIVE_TYPE> valueConsumer, Class<?> fieldType) {
        return new PropertyInputBox<>(width, height, propertyName, offsetX, fontSize, stringOffsetY, null, fields, values, s -> {}, Collections.singletonList(fieldType)) {
            @Override
            public void setSetting() {
                for (int i = 0; i < fields.size(); i++) {
                    valueConsumer.accept((PRIMITIVE_TYPE) ConverterUtils.getConverter(fieldType).fromString(inputBoxes.get(i).getString()));
                }
            }
        };
    }
}