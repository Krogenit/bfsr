package net.bfsr.editor.gui.property;

import net.bfsr.editor.gui.builder.ComponentBuilder;
import net.bfsr.editor.property.PropertiesBuilder;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector2f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButtonColors;
import static net.bfsr.editor.gui.component.MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET;

public class SimplePropertyList<PRIMITIVE_TYPE> extends PropertyList<PropertyComponent, PRIMITIVE_TYPE> {
    private final PropertyGuiElementType propertyGuiElementType;
    private final String propertyName;
    final List<PRIMITIVE_TYPE> objects = new ArrayList<>();

    public SimplePropertyList(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX,
                              int stringOffsetY, Supplier<PRIMITIVE_TYPE> supplier, Object object, List<Field> fields,
                              Object[] values, PropertyGuiElementType propertyGuiElementType, String propertyName,
                              BiConsumer<Object, Integer> valueSetterConsumer) {
        super(width, height, name, fontType, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values,
                valueSetterConsumer);
        this.propertyGuiElementType = propertyGuiElementType;
        this.propertyName = propertyName;
    }

    @Override
    protected PRIMITIVE_TYPE createObject() {
        return supplier.get();
    }

    @Override
    public void add(PRIMITIVE_TYPE arrayElement) {
        try {
            if (arrayElement instanceof PropertiesHolder) {
                PropertiesBuilder.createGuiProperties(arrayElement, baseWidth - MINIMIZABLE_STRING_X_OFFSET, baseHeight, fontType,
                        fontSize, MINIMIZABLE_STRING_X_OFFSET, stringOffsetY, this::addProperty, propertyName);
            } else {
                addProperty(ComponentBuilder.build(propertyGuiElementType, baseWidth - MINIMIZABLE_STRING_X_OFFSET, baseHeight,
                        propertyName, MINIMIZABLE_STRING_X_OFFSET, fontType, fontSize, stringOffsetY, fields,
                        new Object[]{arrayElement}, arrayElement, (o, integer) -> ((List) values[0]).set(integer, o)));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void remove(PropertyComponent propertyComponent) {
        super.remove(propertyComponent);
        objects.remove((PRIMITIVE_TYPE) propertyComponent.object);
    }

    private void addProperty(PropertyComponent propertyComponent) {
        propertyComponent.setOnRightClickSupplier(() -> {
            if (!propertyComponent.isMouseHover()) return false;
            String addString = "Remove";
            Vector2f mousePos = Engine.mouse.getPosition();
            Button button = new Button(null, (int) mousePos.x, (int) mousePos.y,
                    fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset, baseHeight,
                    addString, fontType, fontSize, 4, stringOffsetY, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            setupContextMenuButtonColors(button);
            button.setOnMouseClickRunnable(() -> remove(propertyComponent));
            gui.openContextMenu(button);
            return true;
        });

        addConcealableObject(propertyComponent);
        properties.add(propertyComponent);
        objects.add((PRIMITIVE_TYPE) propertyComponent.object);
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