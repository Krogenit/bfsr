package net.bfsr.editor.gui.component;

import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.editor.property.PropertiesBuilder;
import net.bfsr.editor.property.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.util.RunnableUtils;
import org.joml.Vector2f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.ColorScheme.setupColors;
import static net.bfsr.editor.gui.ColorScheme.setupContextMenuButtonColors;

public class PropertyObjectArray<V extends PropertiesHolder> extends PropertyArray<V, MinimizableHolder<V>, V> {
    public PropertyObjectArray(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX, int stringOffsetY, Supplier<V> supplier, V object,
                               List<Field> fields, Object[] values) {
        super(width, height, name, fontType, fontSize, propertyOffsetX, 0, stringOffsetY, supplier, object, fields, values);
    }

    @Override
    protected V createObject() {
        V propertiesHolder = supplier.get();
        propertiesHolder.setDefaultValues();
        return propertiesHolder;
    }

    @Override
    public MinimizableHolder<V> add(V propertiesHolder) {
        propertiesHolder.clearListeners();
        MinimizableHolder<V> minimizableHolder = new MinimizableHolder<>(baseWidth - propertyOffsetX, baseHeight, propertiesHolder.getName(), fontType, fontSize,
                stringOffsetY, propertiesHolder);
        minimizableHolder.setOnRightClickSupplier(() -> {
            if (!minimizableHolder.isMouseHover()) return false;
            String addString = "Remove";
            Vector2f mousePos = Engine.mouse.getPosition();
            Button button = new Button(null, (int) mousePos.x, (int) mousePos.y, fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset, baseHeight,
                    addString, fontType, fontSize, 4, stringOffsetY, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            setupContextMenuButtonColors(button);
            button.setOnMouseClickRunnable(() -> remove(minimizableHolder));
            gui.openContextMenu(button);
            return true;
        });
        minimizableHolder.setOnMaximizeRunnable(this::updatePositions);
        minimizableHolder.setOnMinimizeRunnable(this::updatePositions);

        propertiesHolder.registerChangeNameEventListener(minimizableHolder::setName);

        int subPropertiesOffsetX = MinimizableHolder.MINIMIZABLE_STRING_X_OFFSET;
        PropertiesBuilder.createGuiProperties(propertiesHolder, baseWidth - propertyOffsetX - subPropertiesOffsetX, baseHeight,
                fontType, fontSize, propertyOffsetX, stringOffsetY, minimizableHolder::addSubObject);

        setupColors(minimizableHolder);
        addSubObject(minimizableHolder);
        propertiesHolders.add(minimizableHolder);
        return minimizableHolder;
    }

    @Override
    protected void updatePropertiesOffset(MinimizableHolder<V> guiObject) {
        List<AbstractGuiObject> guiObjects = guiObject.getSubObjects();
        int maxStringWidth = ((PropertyComponent<?>) guiObjects.get(0)).getStringObject().getWidth();
        for (int i = 1; i < guiObjects.size(); i++) {
            PropertyComponent<?> propertyComponent = (PropertyComponent<?>) guiObjects.get(i);
            maxStringWidth = Math.max(maxStringWidth, propertyComponent.getStringObject().getWidth());
        }

        int propertyOffsetX = maxStringWidth;

        for (int i = 0; i < guiObjects.size(); i++) {
            PropertyComponent<?> propertyComponent = (PropertyComponent<?>) guiObjects.get(i);
            propertyComponent.setPropertyOffsetX(propertyOffsetX);
            propertyComponent.setWidth(baseWidth - this.propertyOffsetX - MinimizableHolder.MINIMIZABLE_STRING_X_OFFSET);
        }
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        List<V> objects = new ArrayList<>(propertiesHolders.size());
        for (int i = 0; i < propertiesHolders.size(); i++) {
            MinimizableHolder<V> minimizableHolder = propertiesHolders.get(i);
            List<AbstractGuiObject> propertiesSubObjects = minimizableHolder.getSubObjects();
            for (int i1 = 0; i1 < propertiesSubObjects.size(); i1++) {
                PropertyComponent<?> abstractGuiObject = (PropertyComponent<?>) propertiesSubObjects.get(i1);
                abstractGuiObject.setSetting();
            }

            objects.add(minimizableHolder.getObject());
            minimizableHolder.setName(minimizableHolder.getObject().getName());
        }

        fields.get(0).set(object, objects);
    }
}