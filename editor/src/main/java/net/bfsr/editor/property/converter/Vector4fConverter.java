package net.bfsr.editor.property.converter;

import org.joml.Vector4f;

class Vector4fConverter extends VectorConverter<Vector4f> {
    Vector4fConverter() {
        super(floats -> new Vector4f(floats[0], floats[1], floats[2], floats[3]),
                vector -> new float[]{vector.x, vector.y, vector.z, vector.w});
    }
}