package net.bfsr.editor.property.converter;

import org.joml.Vector2f;

class Vector2fConverter extends VectorConverter<Vector2f> {
    Vector2fConverter() {
        super(floats -> new Vector2f(floats[0], floats[1]), vector -> new float[]{vector.x, vector.y});
    }
}