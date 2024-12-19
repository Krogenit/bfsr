package net.bfsr.editor.gui.property;

import lombok.Getter;
import net.bfsr.editor.property.PropertiesBuilder;
import net.bfsr.engine.renderer.font.Font;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Getter
public class PropertyObject<PROPERTY_TYPE extends PropertyComponent> extends PropertyComponent {
    final List<PROPERTY_TYPE> properties = new ArrayList<>();

    public PropertyObject(int width, int height, String name, Font font, int fontSize, int propertyOffsetX, int stringOffsetY,
                          Object object, List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, font, fontSize, propertyOffsetX + MINIMIZABLE_STRING_X_OFFSET, 1, MINIMIZABLE_STRING_X_OFFSET,
                stringOffsetY, object, fields, values, valueConsumer);
        setCanMaximize(true);

        PropertiesBuilder.createGuiProperties(values[0], width - MINIMIZABLE_STRING_X_OFFSET, height, font, fontSize,
                MINIMIZABLE_STRING_X_OFFSET, stringOffsetY, propertyComponent -> addProperty((PROPERTY_TYPE) propertyComponent));
    }

    void addProperty(PROPERTY_TYPE propertyComponent) {
        properties.add(propertyComponent);
        add(propertyComponent);
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        for (int i = 0; i < properties.size(); i++) {
            PROPERTY_TYPE propertyComponent = properties.get(i);
            propertyComponent.setSetting();
        }
    }

    @Override
    protected void updateHeight() {
        if (maximized) {
            setHeight(getMaximizedHeight());
        } else {
            setHeight(baseHeight);
        }
    }

    protected int getMaximizedHeight() {
        int height = baseHeight;
        for (int i = 0; i < properties.size(); i++) {
            height += properties.get(i).getHeight();
        }

        return height;
    }

    @Override
    public void updateConcealableObjectsPositions() {
        if (properties.isEmpty()) return;

        int maxStringWidth = properties.get(0).getLabel().getWidth();
        for (int i = 1; i < properties.size(); i++) {
            PropertyComponent propertyComponent = properties.get(i);
            maxStringWidth = Math.max(maxStringWidth, propertyComponent.getLabel().getWidth());
        }

        int propertyOffsetX = maxStringWidth;
        int height = -baseHeight;
        for (int i = 0; i < properties.size(); i++) {
            PropertyComponent propertyComponent = properties.get(i);
            propertyComponent.atTopLeft(MINIMIZABLE_STRING_X_OFFSET, height + propertyOffsetY);
            propertyComponent.setPropertyOffsetX(propertyOffsetX);
            height -= propertyComponent.getHeight();
        }

        updateHeight();
    }
}