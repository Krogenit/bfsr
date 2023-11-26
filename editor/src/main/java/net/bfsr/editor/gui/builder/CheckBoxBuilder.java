package net.bfsr.editor.gui.builder;

import net.bfsr.editor.gui.property.PropertyCheckBox;
import net.bfsr.editor.gui.property.PropertyComponent;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

public class CheckBoxBuilder extends ComponentBuilder {
    @Override
    public PropertyComponent build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize,
                                   int stringOffsetY, List<Field> fields, Object[] values, Object object,
                                   BiConsumer<Object, Integer> valueSetterConsumer) {
        return new PropertyCheckBox(width, height, propertyName, offsetX, fontSize, stringOffsetY, object, fields, values,
                valueSetterConsumer);
    }
}