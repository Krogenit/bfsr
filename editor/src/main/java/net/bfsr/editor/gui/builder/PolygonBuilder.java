package net.bfsr.editor.gui.builder;

import net.bfsr.editor.gui.property.PolygonProperty;
import net.bfsr.editor.gui.property.SimplePropertyList;
import net.bfsr.editor.property.Property;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class PolygonBuilder extends ListBuilder {
    @Override
    protected SimplePropertyList createProperty(int width, int height, String propertyName, int offsetX, String fontName, int fontSize,
                                                int stringOffsetY, List<Field> fields, Object[] values, Object object,
                                                BiConsumer<Object, Integer> valueSetterConsumer, Property annotation, Supplier supplier) {
        return new PolygonProperty(width, height, propertyName, fontName, fontSize, offsetX, stringOffsetY, supplier, object, fields,
                values, annotation.arrayElementType(), annotation.arrayElementName(), valueSetterConsumer);
    }
}