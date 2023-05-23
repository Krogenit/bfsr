package net.bfsr.editor.gui.component;

import net.bfsr.client.gui.button.Button;
import net.bfsr.editor.gui.builder.ComponentBuilder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.property.PropertyGuiElementType;
import net.bfsr.util.RunnableUtils;
import org.joml.Vector2f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.ColorScheme.setupContextMenuButtonColors;

public class PropertyPrimitiveArray<V extends PropertiesHolder, PRIMITIVE_TYPE> extends PropertyArray<V, PropertyComponent<?>, PRIMITIVE_TYPE> {
    private final PropertyGuiElementType propertyGuiElementType;
    private final String propertyName;
    private final List<PRIMITIVE_TYPE> objects = new ArrayList<>();

    public PropertyPrimitiveArray(int width, int height, String name, FontType fontType, int fontSize, int propertyOffsetX, int stringOffsetY, Supplier<PRIMITIVE_TYPE> supplier,
                                  V object, List<Field> fields, Object[] values, PropertyGuiElementType propertyGuiElementType, String propertyName) {
        super(width, height, name, fontType, fontSize, propertyOffsetX, 0, stringOffsetY, supplier, object, fields, values);
        this.propertyGuiElementType = propertyGuiElementType;
        this.propertyName = propertyName;
    }

    @Override
    protected PRIMITIVE_TYPE createObject() {
        return supplier.get();
    }

    @Override
    public PropertyComponent<V> add(PRIMITIVE_TYPE arrayElement) {
        try {
            PropertyComponent<V> propertyComponent = ComponentBuilder.build(propertyGuiElementType, baseWidth - propertyOffsetX, baseHeight, propertyName,
                    fontType.getStringCache().getStringWidth(propertyName, fontSize), fontType,
                    fontSize, stringOffsetY, fields, new Object[]{arrayElement}, (Consumer<PRIMITIVE_TYPE>) objects::add, arrayElement.getClass());
            propertyComponent.setOnRightClickSupplier(() -> {
                if (!propertyComponent.isMouseHover()) return false;
                String addString = "Remove";
                Vector2f mousePos = Engine.mouse.getPosition();
                Button button = new Button(null, (int) mousePos.x, (int) mousePos.y, fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset, baseHeight,
                        addString, fontType, fontSize, 4, stringOffsetY, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
                setupContextMenuButtonColors(button);
                button.setOnMouseClickRunnable(() -> remove(propertyComponent));
                gui.openContextMenu(button);
                return true;
            });

            addSubObject(propertyComponent);
            propertiesHolders.add(propertyComponent);
            return propertyComponent;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setRepositionConsumerForSubObjects() {
        int height = 0;

        for (int i = 0; i < propertiesHolders.size(); i++) {
            PropertyComponent<?> propertyComponent = propertiesHolders.get(i);
            subObjectsRepositionConsumer.setup(propertyComponent, propertyOffsetX, height);
            propertyComponent.setWidth(baseWidth - propertyOffsetX);
            height += propertyComponent.getHeight();
        }

        this.height = baseHeight + height;
    }

    @Override
    protected void updatePropertiesOffset(PropertyComponent<?> guiObject) {

    }

    @Override
    public void setSetting() throws IllegalAccessException {
        objects.clear();

        for (int i = 0; i < propertiesHolders.size(); i++) {
            PropertyComponent<?> propertyComponent = propertiesHolders.get(i);
            propertyComponent.setSetting();
        }

        fields.get(0).set(object, objects);
    }
}