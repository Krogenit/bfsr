package net.bfsr.editor.gui.property;

import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.font.string.StringOffsetType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;

public class MinimizablePropertyList extends PropertyList<PropertyObject<PropertyComponent>, PropertiesHolder> {
    private final String elementPropertyName;

    public MinimizablePropertyList(int width, int height, String name, Font font, int fontSize, int propertyOffsetX,
                                   int stringOffsetY, Supplier<PropertiesHolder> supplier, Object object, List<Field> fields,
                                   Object[] values, BiConsumer<Object, Integer> valueConsumer, String elementPropertyName,
                                   Runnable changeValueListener) {
        super(width, height, name, font, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values, valueConsumer,
                changeValueListener);
        this.elementPropertyName = elementPropertyName;
    }

    @Override
    protected PropertiesHolder createObject() {
        PropertiesHolder propertiesHolder = supplier.get();
        propertiesHolder.setDefaultValues();
        return propertiesHolder;
    }

    @Override
    public void addObjectAt(int index, PropertiesHolder object) {
        object.clearListeners();
        PropertyObject<PropertyComponent> propertyObject = new PropertyObject<>(baseWidth - MINIMIZABLE_STRING_X_OFFSET, baseHeight,
                elementPropertyName, font, fontSize, MINIMIZABLE_STRING_X_OFFSET, stringOffsetY, object, fields,
                new Object[]{object}, valueConsumer, changeValueListener);
        propertyObject.setRightReleaseConsumer((mouseX, mouseY) -> {
            String addString = "Remove";
            Button button = new Button(font.getWidth(addString, fontSize) + contextMenuStringXOffset, baseHeight, addString, font, fontSize,
                    4, stringOffsetY, StringOffsetType.DEFAULT, (mouseX1, mouseY1) -> removeProperty(propertyObject));
            guiManager.openContextMenu(setupContextMenuButton(button).atBottomLeft(mouseX, mouseY - baseHeight));
        });
        object.addChangeNameEventListener(propertyObject::setName);

        if (index == properties.size() || properties.isEmpty()) {
            properties.add(propertyObject);
            add(propertyObject);
        } else {
            addAt(hideableObjects.indexOf(properties.get(index)), propertyObject);
            properties.add(index, propertyObject);
        }
    }

    @Override
    public void addObject(PropertiesHolder object) {
        addObjectAt(properties.size(), object);
    }

    @Override
    public void removeObject(PropertiesHolder object) {
        for (int i = 0; i < properties.size(); i++) {
            PropertyObject<PropertyComponent> propertyObject = properties.get(i);
            if (propertyObject.getObject() == object) {
                properties.remove(i);
                remove(propertyObject);
                return;
            }
        }
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        List<PropertiesHolder> objects = new ArrayList<>(properties.size());
        for (int i = 0; i < properties.size(); i++) {
            PropertyObject<PropertyComponent> propertyObject = properties.get(i);
            propertyObject.setSetting();
            objects.add((PropertiesHolder) propertyObject.getObject());
        }

        valueConsumer.accept(objects, 0);
    }
}