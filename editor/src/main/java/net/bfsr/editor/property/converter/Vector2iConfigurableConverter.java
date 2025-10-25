package net.bfsr.editor.property.converter;

import net.bfsr.engine.config.Vector2iConfigurable;

class Vector2iConfigurableConverter extends VectorConverter<Vector2iConfigurable> {
    Vector2iConfigurableConverter() {
        super(floats -> new Vector2iConfigurable((int) floats[0], (int) floats[1]), vector -> new float[]{vector.x(), vector.y()});
    }
}