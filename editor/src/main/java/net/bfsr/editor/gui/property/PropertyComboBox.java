package net.bfsr.editor.gui.property;

import net.bfsr.engine.gui.component.ComboBox;
import net.bfsr.engine.gui.component.CompoBoxElement;
import net.bfsr.engine.gui.object.SimpleGuiObject;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

import static net.bfsr.editor.gui.EditorTheme.*;

public class PropertyComboBox extends PropertyComponent {
    public PropertyComboBox(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY,
                            Object object, List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueSetterConsumer) {
        super(width, height, name, FONT_TYPE, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values,
                valueSetterConsumer);

        Field field = fields.get(0);
        Class<?> type = field.getType();
        if (Enum.class.isAssignableFrom(type)) {
            ComboBox<Enum<?>> comboBox = new ComboBox<>(width - propertyOffsetX, height);
            addSubObject(comboBox);
            Enum<?> enumValue = (Enum<?>) values[0];
            Enum<?>[] enums = enumValue.getClass().getEnumConstants();
            for (int i = 0; i < enums.length; i++) {
                Enum<?> value = enums[i];
                comboBox.addSubObject(new CompoBoxElement<>(width - propertyOffsetX, height, value, value.toString(), FONT_TYPE,
                        fontSize, stringOffsetY, comboBox));
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
    public void setSetting() {
        valueConsumer.accept(((ComboBox<?>) subObjects.get(0)).getSelectedValue(), 0);
    }

    @Override
    public SimpleGuiObject setWidth(int width) {
        subObjects.get(0).setWidth(width - propertyOffsetX);
        return super.setWidth(width);
    }
}