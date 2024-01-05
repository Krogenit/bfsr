package net.bfsr.editor.property.converter;

import net.bfsr.config.Vector2fConfigurable;

class Vector2fConfigurableConverter extends VectorConverter<Vector2fConfigurable> {
    Vector2fConfigurableConverter() {
        super(floats -> new Vector2fConfigurable(floats[0], floats[1]), vector -> new float[]{vector.x(), vector.y()});
    }
}