package net.bfsr.editor.gui.property;

import net.bfsr.editor.property.PropertiesHolder;
import net.bfsr.engine.gui.component.CheckBox;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.List;

import static net.bfsr.editor.gui.ColorScheme.*;

public class PropertyCheckBox<V extends PropertiesHolder> extends PropertyComponent<V> {
    private final CheckBox checkBox;

    public PropertyCheckBox(int width, int height, String name, int offsetX, int fontSize, int stringYOffset, V object,
                            List<Field> fields, Object[] values) {
        super(new CheckBox(null, height - 2, height - 2, (boolean) values[0]), width, height, name, FontType.CONSOLA, fontSize,
                offsetX, 1, stringYOffset, object, fields, values);
        checkBox = (CheckBox) subObjects.get(0);
        checkBox.setColor(CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, 1.0f);
        checkBox.setHoverColor(CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, 1.0f);
        checkBox.setOutlineColor(INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, 1.0f);
        checkBox.setOutlineHoverColor(INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY,
                INPUT_OUTLINE_HOVER_COLOR_GRAY, 1.0f);
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        fields.get(0).set(object, checkBox.isChecked());
    }
}