package net.bfsr.editor.gui.property;

import net.bfsr.engine.gui.component.ComboBox;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

import static net.bfsr.editor.gui.EditorTheme.FONT;
import static net.bfsr.editor.gui.EditorTheme.setup;

public class PropertyComboBox extends PropertyComponent {
    private ComboBox<Enum<?>> comboBox;

    public PropertyComboBox(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY, Object object,
                            List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueConsumer, Runnable changeValueListener) {
        super(width, height, name, FONT, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values,
                valueConsumer, changeValueListener);

        Field field = fields.get(0);
        Class<?> type = field.getType();
        if (Enum.class.isAssignableFrom(type)) {
            addNonConcealable(comboBox = new ComboBox<>(width - propertyOffsetX, height, FONT, fontSize, stringOffsetY));
            Enum<?> enumValue = (Enum<?>) values[0];
            Enum<?>[] enums = enumValue.getClass().getEnumConstants();
            for (int i = 0; i < enums.length; i++) {
                comboBox.addData(enums[i]);
            }

            setup(comboBox);
            comboBox.setSelectedIndex(enumValue.ordinal());
        }
    }

    @Override
    public void setSetting() {
        valueConsumer.accept(comboBox.getSelectedValue(), 0);
    }

    @Override
    public void updateConcealableObjectsPositions() {
        comboBox.atTopLeft(propertyOffsetX, propertyOffsetY);
        comboBox.setWidth(width - propertyOffsetX);
    }
}