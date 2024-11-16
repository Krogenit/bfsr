package net.bfsr.editor.gui.property;

import net.bfsr.client.Client;
import net.bfsr.editor.property.converter.ConverterUtils;
import net.bfsr.editor.property.converter.PropertyConverter;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector2f;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;

public class PropertyMap<KEY> extends PropertyList<PropertyObject<PropertyComponent>, PropertiesHolder> {
    private final Supplier<KEY> keySupplier;

    public PropertyMap(int width, int height, String name, Font font, int fontSize, int propertyOffsetX, int stringOffsetY,
                       Object object, List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueConsumer,
                       Supplier<KEY> keySupplier, Supplier<PropertiesHolder> supplier) {
        super(width, height, name, font, fontSize, propertyOffsetX, stringOffsetY, supplier, object, fields, values, valueConsumer);
        this.keySupplier = keySupplier;
    }

    @Override
    protected PropertiesHolder createObject() {
        PropertiesHolder propertiesHolder = supplier.get();
        propertiesHolder.setDefaultValues();
        return propertiesHolder;
    }

    @Override
    public void addProperty(PropertiesHolder propertiesHolder) {
        add(keySupplier.get(), propertiesHolder);
    }

    public void add(KEY key, PropertiesHolder propertiesHolder) {
        PropertyObject<PropertyComponent> component = new PropertyObject<>(baseWidth - MINIMIZABLE_STRING_X_OFFSET, baseHeight,
                key.toString(), font, fontSize, MINIMIZABLE_STRING_X_OFFSET, stringOffsetY, propertiesHolder, null,
                new Object[]{propertiesHolder}, (o, integer) -> {});

        component.addProperty(new PropertyInputBox(width - MINIMIZABLE_STRING_X_OFFSET, baseHeight, "keyName",
                MINIMIZABLE_STRING_X_OFFSET, fontSize, stringOffsetY, key, fields, new Object[]{key}, List.of(key.getClass()),
                (o, integer) -> {}) {
            @Override
            public void setSetting() {
                component.setName(inputBoxes.get(0).getString());
            }
        });

        component.setRightClickRunnable(() -> {
            String addString = "Remove";
            Vector2f mousePos = Engine.mouse.getPosition();
            Button button = new Button((int) mousePos.x, (int) mousePos.y,
                    font.getGlyphsBuilder().getWidth(addString, fontSize) + contextMenuStringXOffset, baseHeight,
                    addString, font, fontSize, 4, stringOffsetY, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            setupContextMenuButton(button);
            button.setLeftReleaseRunnable(() -> remove(component));
            Client.get().getGuiManager().openContextMenu(button);
        });

        properties.add(component);
        add(component);
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        Map<KEY, Object> propertiesHolderMap = new HashMap<>();
        for (int i = 0; i < properties.size(); i++) {
            PropertyObject<PropertyComponent> propertyObject = properties.get(i);
            try {
                propertyObject.setSetting();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            Class<KEY> keyClass = (Class<KEY>) keySupplier.get().getClass();
            PropertyConverter<KEY> converter = (PropertyConverter<KEY>) ConverterUtils.getConverter(keyClass);
            KEY key = converter.fromString(keyClass, propertyObject.getName());
            propertiesHolderMap.put(key, propertyObject.getObject());
        }

        fields.get(0).set(object, propertiesHolderMap);
    }
}