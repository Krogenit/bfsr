package net.bfsr.editor.gui.property;

import net.bfsr.editor.gui.builder.ComponentBuilder;
import net.bfsr.editor.property.PropertiesBuilder;
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

public class SimplePropertyList<PRIMITIVE_TYPE> extends PropertyList<PropertyComponent, PRIMITIVE_TYPE> {
    private final PropertyGuiElementType propertyGuiElementType;
    private final String propertyName;
    final List<PRIMITIVE_TYPE> objects = new ArrayList<>();

    public SimplePropertyList(int width, int height, String name, String fontName, int fontSize, int propertyOffsetX, int stringOffsetY,
                              Supplier<PRIMITIVE_TYPE> supplier, Object object, List<Field> fields, Object[] values,
                              PropertyGuiElementType propertyGuiElementType, String propertyName,
                              BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, fontName, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values, valueConsumer);
        this.propertyGuiElementType = propertyGuiElementType;
        this.propertyName = propertyName;
    }

    @Override
    protected PRIMITIVE_TYPE createObject() {
        PRIMITIVE_TYPE primitiveType = supplier.get();
        if (primitiveType instanceof PropertiesHolder propertiesHolder) {
            propertiesHolder.setDefaultValues();
        }

        return primitiveType;
    }

    @Override
    public void addProperty(PRIMITIVE_TYPE arrayElement) {
        try {
            if (arrayElement instanceof PropertiesHolder) {
                PropertiesBuilder.createGuiProperties(arrayElement, baseWidth - MINIMIZABLE_STRING_X_OFFSET, baseHeight,
                        fontName, fontSize, MINIMIZABLE_STRING_X_OFFSET, stringOffsetY, this::addPropertyComponent, propertyName);
            } else {
                addPropertyComponent(ComponentBuilder.build(propertyGuiElementType, baseWidth - MINIMIZABLE_STRING_X_OFFSET,
                        baseHeight, propertyName, Engine.getFontManager().getFont(fontName).getWidth(propertyName, fontSize),
                        fontName, fontSize, stringOffsetY, fields, new Object[]{arrayElement}, arrayElement,
                        (o, integer) -> ((List) values[0]).set(integer, o)));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void removeProperty(PropertyComponent propertyComponent) {
        super.removeProperty(propertyComponent);
        objects.remove((PRIMITIVE_TYPE) propertyComponent.object);
    }

    private void addPropertyComponent(PropertyComponent propertyComponent) {
        propertyComponent.setRightClickConsumer((mouseX, mouseY) -> {
            String addString = "Remove";
            Button button = new Button(mouseX, mouseY, Engine.getFontManager().getFont(fontName)
                    .getWidth(addString, fontSize) + contextMenuStringXOffset, baseHeight, addString, fontName, fontSize, 4, stringOffsetY,
                    StringOffsetType.DEFAULT, EMPTY_BI_CONSUMER);
            setupContextMenuButton(button);
            button.setLeftReleaseConsumer((mouseX1, mouseY1) -> removeProperty(propertyComponent));
            guiManager.openContextMenu(button);
        });

        properties.add(propertyComponent);
        objects.add((PRIMITIVE_TYPE) propertyComponent.object);
        add(propertyComponent);
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        for (int i = 0; i < properties.size(); i++) {
            PropertyComponent propertyComponent = properties.get(i);
            propertyComponent.setSetting();
        }

        valueConsumer.accept(objects, 0);
    }
}