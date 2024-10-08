package net.bfsr.editor.gui.builder;

import net.bfsr.editor.gui.property.PropertyComponent;
import net.bfsr.editor.gui.property.PropertyFileSelector;
import net.bfsr.engine.renderer.font.Font;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

class FileSelectorBuilder extends ComponentBuilder {
    @Override
    public PropertyComponent build(int width, int height, String propertyName, int offsetX, Font font, int fontSize,
                                   int stringOffsetY, List<Field> fields, Object[] values, Object object,
                                   BiConsumer<Object, Integer> valueSetterConsumer) {
        return new PropertyFileSelector(width, height, propertyName, offsetX, fontSize, stringOffsetY, object, fields, values,
                valueSetterConsumer);
    }
}