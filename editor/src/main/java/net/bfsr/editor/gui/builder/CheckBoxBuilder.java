package net.bfsr.editor.gui.builder;

import net.bfsr.client.renderer.font.FontType;
import net.bfsr.editor.gui.component.PropertyCheckBox;
import net.bfsr.editor.gui.component.PropertyComponent;
import net.bfsr.property.PropertiesHolder;

import java.lang.reflect.Field;
import java.util.List;

public class CheckBoxBuilder extends ComponentBuilder {
    @Override
    public <V extends PropertiesHolder> PropertyComponent<V> build(int width, int height, String propertyName, int xOffset, FontType fontType, int fontSize, int stringOffsetY,
                                                                   List<Field> fields, Object[] values, V object) {
        return new PropertyCheckBox<>(width, height, propertyName, xOffset, fontSize, stringOffsetY, object, fields, values);
    }
}