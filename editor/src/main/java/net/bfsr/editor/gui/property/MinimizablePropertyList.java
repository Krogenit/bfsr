package net.bfsr.editor.gui.property;

import net.bfsr.editor.gui.component.MinimizableGuiObject;
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

public class MinimizablePropertyList extends PropertyList<PropertyObject<PropertyComponent>, PropertiesHolder> {
    private final String elementPropertyName;

    public MinimizablePropertyList(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX,
                                   int stringOffsetY, Supplier<PropertiesHolder> supplier, Object object,
                                   List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueSetterConsumer,
                                   String elementPropertyName) {
        super(width, height, name, fontType, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values,
                valueSetterConsumer);
        this.elementPropertyName = elementPropertyName;
    }

    @Override
    protected PropertiesHolder createObject() {
        PropertiesHolder propertiesHolder = supplier.get();
        propertiesHolder.setDefaultValues();
        return propertiesHolder;
    }

    @Override
    public void add(PropertiesHolder propertiesHolder) {
        propertiesHolder.clearListeners();
        PropertyObject<PropertyComponent> propertyObject = new PropertyObject<>(
                baseWidth - MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET, baseHeight,
                elementPropertyName, fontType, fontSize, MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET,
                stringOffsetY, propertiesHolder, fields, new Object[]{propertiesHolder}, valueConsumer);
        propertyObject.setOnRightClickSupplier(() -> {
            if (!propertyObject.isMouseHover()) return false;
            String addString = "Remove";
            Vector2f mousePos = Engine.mouse.getPosition();
            Button button = new Button(null, (int) mousePos.x, (int) mousePos.y,
                    fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset, baseHeight,
                    addString, fontType, fontSize, 4, stringOffsetY, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            setupContextMenuButtonColors(button);
            button.setOnMouseClickRunnable(() -> remove(propertyObject));
            gui.openContextMenu(button);
            return true;
        });
        propertiesHolder.registerChangeNameEventListener(propertyObject::setName);

        addConcealableObject(propertyObject);
        properties.add(propertyObject);
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