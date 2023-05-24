package net.bfsr.editor.gui.builder;

import net.bfsr.editor.gui.component.PropertyComboBox;
import net.bfsr.editor.gui.component.PropertyComponent;
import net.bfsr.editor.property.PropertiesHolder;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.List;

public class ComboBoxBuilder extends ComponentBuilder {
    @Override
    public <V extends PropertiesHolder> PropertyComponent<V> build(int width, int height, String propertyName, int offsetX, FontType fontType, int fontSize, int stringOffsetY,
                                                                   List<Field> fields, Object[] values, V object) {
        return new PropertyComboBox<>(width, height, propertyName, offsetX, fontSize, stringOffsetY, object, fields, values);
    }
}