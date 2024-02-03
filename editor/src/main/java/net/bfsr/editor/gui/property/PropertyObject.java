package net.bfsr.editor.gui.property;

import lombok.Getter;
import net.bfsr.editor.property.PropertiesBuilder;
import net.bfsr.engine.renderer.font.FontType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static net.bfsr.editor.gui.component.MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET;

@Getter
public class PropertyObject<T extends PropertyComponent> extends MinimizablePropertyComponent {
    final List<T> properties = new ArrayList<>();

    public PropertyObject(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX,
                          int stringOffsetY, Object object, List<Field> fields, Object[] values,
                          BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, fontType, fontSize, propertyOffsetX, 1, stringOffsetY, object, fields, values,
                valueConsumer);

        PropertiesBuilder.createGuiProperties(values[0], width - MINIMIZABLE_STRING_X_OFFSET, height, fontType, fontSize,
                MINIMIZABLE_STRING_X_OFFSET, stringOffsetY, propertyComponent -> add((T) propertyComponent));
    }

    protected void add(T propertyComponent) {
        addConcealableObject(propertyComponent);
        properties.add(propertyComponent);
        updatePropertiesOffset();
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        for (int i = 0; i < properties.size(); i++) {
            T propertyComponent = properties.get(i);
            propertyComponent.setSetting();
        }
    }

    @Override
    protected void setRepositionConsumerForSubObjects() {
        int height = 0;
        for (int i = 0; i < properties.size(); i++) {
            T guiObject = properties.get(i);
            subObjectsRepositionConsumer.setup(guiObject, MINIMIZABLE_STRING_X_OFFSET, height + propertyYOffset);
            height += guiObject.getHeight();
        }

        if (maximized) {
            this.height = baseHeight + height;
        } else {
            this.height = baseHeight;
        }
    }

    @Override
    public void updatePositionAndSize() {
        super.updatePositionAndSize();
        updatePropertiesOffset();
    }

    void updatePropertiesOffset() {
        if (properties.size() == 0) return;

        int maxStringWidth = properties.get(0).getStringObject().getWidth();
        for (int i = 1; i < properties.size(); i++) {
            PropertyComponent propertyComponent = properties.get(i);
            maxStringWidth = Math.max(maxStringWidth, propertyComponent.getStringObject().getWidth());
        }

        int propertyOffsetX = maxStringWidth;

        for (int i = 0; i < properties.size(); i++) {
            PropertyComponent propertyComponent = properties.get(i);
            propertyComponent.setPropertyOffsetX(propertyOffsetX);
            propertyComponent.updatePositionAndSize();
        }
    }
}