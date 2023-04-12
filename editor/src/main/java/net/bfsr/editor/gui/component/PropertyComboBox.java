package net.bfsr.editor.gui.component;

import net.bfsr.client.gui.SimpleGuiObject;
import net.bfsr.client.gui.component.ComboBox;
import net.bfsr.client.gui.component.CompoBoxElement;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.property.PropertiesHolder;

import java.lang.reflect.Field;
import java.util.List;

import static net.bfsr.editor.gui.ColorScheme.*;

public class PropertyComboBox<V extends PropertiesHolder> extends PropertyComponent<V> {
    public PropertyComboBox(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY, V object, List<Field> fields, Object[] values) {
        super(width, height, name, FontType.CONSOLA, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values);

        Field field = fields.get(0);
        Class<?> type = field.getType();
        if (Enum.class.isAssignableFrom(type)) {
            ComboBox<Enum<?>> comboBox = new ComboBox<>(width - propertyOffsetX, height);
            addSubObject(comboBox);
            Enum<?> enumValue = (Enum<?>) values[0];
            Enum<?>[] enums = enumValue.getClass().getEnumConstants();
            for (int i = 0; i < enums.length; i++) {
                Enum<?> value = enums[i];
                comboBox.addSubObject(new CompoBoxElement<>(width - propertyOffsetX, height, value, value.toString(), FontType.CONSOLA, fontSize, stringOffsetY, comboBox));
            }

            comboBox.setSelectedIndex(enumValue.ordinal());
            comboBox.setColor(BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, 1.0f);
            comboBox.setHoverColor(BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, 1.0f);
            comboBox.setOutlineColor(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f);
            comboBox.setOutlineHoverColor(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f);
            comboBox.setTextColor(TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f);
        }
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        fields.get(0).set(object, ((ComboBox<?>) subObjects.get(0)).getSelectedValue());
    }

    @Override
    public SimpleGuiObject setWidth(int width) {
        subObjects.get(0).setWidth(width - propertyOffsetX);
        return super.setWidth(width);
    }
}