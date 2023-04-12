package net.bfsr.editor.gui.builder;

import net.bfsr.client.renderer.font.FontType;
import net.bfsr.editor.gui.component.PropertyComponent;
import net.bfsr.editor.gui.component.PropertyFileSelector;
import net.bfsr.property.PropertiesHolder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

public class FileSelectorBuilder extends ComponentBuilder {
    @Override
    public <P extends PropertiesHolder> PropertyComponent<P> build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize,
                                                                   int stringOffsetY, List<Field> fields, Object[] values,
                                                                   P object) {
        return new PropertyFileSelector<>(width, height, propertyName, offsetX, fontSize, stringOffsetY, object, fields, values);
    }

    @Override
    public <P extends PropertiesHolder, PRIMITIVE_TYPE> PropertyComponent<P> build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize, int stringOffsetY,
                                                                                   List<Field> fields, Object[] values, Consumer<PRIMITIVE_TYPE> valueConsumer, Class<?> fieldType) {
        return new PropertyFileSelector<>(width, height, propertyName, offsetX, fontSize, stringOffsetY, null, fields, values) {
            @Override
            public void setSetting() {
                valueConsumer.accept((PRIMITIVE_TYPE) path);
            }
        };
    }
}