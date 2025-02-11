package net.bfsr.editor.gui.property;

import lombok.Getter;
import net.bfsr.engine.gui.component.CheckBox;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

import static net.bfsr.editor.gui.EditorTheme.FONT;
import static net.bfsr.editor.gui.EditorTheme.setup;

@Getter
public class PropertyCheckBox extends PropertyComponent {
    private final CheckBox checkBox;

    public PropertyCheckBox(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY, Object object,
                            List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueConsumer, Runnable changeValueListener) {
        super(width, height, name, FONT, fontSize, propertyOffsetX, 1, stringOffsetY, object, fields, values, valueConsumer,
                changeValueListener);
        addNonConcealable(checkBox = new CheckBox(height - 2, height - 2, (boolean) values[0]));
        setup(checkBox).atBottomLeft(propertyOffsetX, propertyOffsetY);
    }

    @Override
    public void setSetting() {
        valueConsumer.accept(checkBox.isChecked(), 0);
    }

    @Override
    public void updateConcealableObjectsPositions() {
        checkBox.atBottomLeft(propertyOffsetX, propertyOffsetY);
    }
}