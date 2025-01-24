package net.bfsr.editor.gui.property;

import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.renderer.font.string.StringOffsetType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;

public class MinimizablePropertyList extends PropertyList<PropertyObject<PropertyComponent>, PropertiesHolder> {
    private final String elementPropertyName;

    public MinimizablePropertyList(int width, int height, String name, String fontName, int fontSize, int propertyOffsetX,
                                   int stringOffsetY, Supplier<PropertiesHolder> supplier, Object object, List<Field> fields,
                                   Object[] values, BiConsumer<Object, Integer> valueConsumer, String elementPropertyName) {
        super(width, height, name, fontName, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values, valueConsumer);
        this.elementPropertyName = elementPropertyName;
    }

    @Override
    protected PropertiesHolder createObject() {
        PropertiesHolder propertiesHolder = supplier.get();
        propertiesHolder.setDefaultValues();
        return propertiesHolder;
    }

    @Override
    public void addProperty(PropertiesHolder propertiesHolder) {
        propertiesHolder.clearListeners();
        PropertyObject<PropertyComponent> propertyObject = new PropertyObject<>(baseWidth - MINIMIZABLE_STRING_X_OFFSET, baseHeight,
                elementPropertyName, fontName, fontSize, MINIMIZABLE_STRING_X_OFFSET, stringOffsetY, propertiesHolder, fields,
                new Object[]{propertiesHolder}, valueConsumer);
        propertyObject.setRightClickConsumer((mouseX, mouseY) -> {
            String addString = "Remove";
            Button button = new Button(mouseX, mouseY - baseHeight, Engine.getFontManager().getFont(fontName)
                    .getWidth(addString, fontSize) + contextMenuStringXOffset, baseHeight, addString, fontName, fontSize, 4, stringOffsetY,
                    StringOffsetType.DEFAULT, EMPTY_BI_CONSUMER);
            setupContextMenuButton(button);
            button.setLeftReleaseConsumer((mouseX1, mouseY1) -> removeProperty(propertyObject));
            guiManager.openContextMenu(button);
        });
        propertiesHolder.addChangeNameEventListener(propertyObject::setName);

        properties.add(propertyObject);
        add(propertyObject);
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