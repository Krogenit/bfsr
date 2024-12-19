package net.bfsr.editor.gui.property;

import net.bfsr.client.Client;
import net.bfsr.editor.gui.builder.ComponentBuilder;
import net.bfsr.editor.property.PropertiesBuilder;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector2f;

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

    public SimplePropertyList(int width, int height, String name, Font font, int fontSize, int propertyOffsetX,
                              int stringOffsetY, Supplier<PRIMITIVE_TYPE> supplier, Object object, List<Field> fields,
                              Object[] values, PropertyGuiElementType propertyGuiElementType, String propertyName,
                              BiConsumer<Object, Integer> valueConsumer) {
        super(width, height, name, font, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values,
                valueConsumer);
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
                PropertiesBuilder.createGuiProperties(arrayElement, baseWidth - MINIMIZABLE_STRING_X_OFFSET, baseHeight, font,
                        fontSize, MINIMIZABLE_STRING_X_OFFSET, stringOffsetY, this::addPropertyComponent, propertyName);
            } else {
                addPropertyComponent(ComponentBuilder.build(propertyGuiElementType, baseWidth - MINIMIZABLE_STRING_X_OFFSET, baseHeight,
                        propertyName, font.getGlyphsBuilder().getWidth(propertyName, fontSize), font, fontSize, stringOffsetY,
                        fields, new Object[]{arrayElement}, arrayElement, (o, integer) -> ((List) values[0]).set(integer, o)));
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
        propertyComponent.setRightClickRunnable(() -> {
            String addString = "Remove";
            Vector2f mousePos = Engine.mouse.getPosition();
            Button button = new Button((int) mousePos.x, (int) mousePos.y,
                    font.getGlyphsBuilder().getWidth(addString, fontSize) + contextMenuStringXOffset, baseHeight,
                    addString, font, fontSize, 4, stringOffsetY, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            setupContextMenuButton(button);
            button.setLeftReleaseRunnable(() -> removeProperty(propertyComponent));
            Client.get().getGuiManager().openContextMenu(button);
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