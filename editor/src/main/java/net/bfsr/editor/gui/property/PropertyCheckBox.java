package net.bfsr.editor.gui.property;

import net.bfsr.engine.gui.component.CheckBox;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

import static net.bfsr.editor.gui.EditorTheme.*;

public class PropertyCheckBox extends PropertyComponent {
    private final CheckBox checkBox;

    public PropertyCheckBox(int width, int height, String name, int propertyOffsetX, int fontSize, int stringYOffset,
                            Object object, List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueSetterConsumer) {
        super(new CheckBox(null, height - 2, height - 2, (boolean) values[0]), width, height, name, FONT_TYPE,
                fontSize,
                propertyOffsetX, 1, stringYOffset, object, fields, values, valueSetterConsumer);
        checkBox = (CheckBox) subObjects.get(0);
        checkBox.setColor(CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, 1.0f);
        checkBox.setHoverColor(CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, 1.0f);
        checkBox.setOutlineColor(INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, 1.0f);
        checkBox.setOutlineHoverColor(INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY,
                INPUT_OUTLINE_HOVER_COLOR_GRAY, 1.0f);
    }

    @Override
    public void setSetting() {
        valueConsumer.accept(checkBox.isChecked(), 0);
    }
}