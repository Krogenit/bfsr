package net.bfsr.math;

import lombok.AllArgsConstructor;
import net.bfsr.entity.TextureObject;

import java.nio.FloatBuffer;

@AllArgsConstructor
public enum ModelMatrixType {
    DEFAULT(Transformation::getDefaultModelMatrix), BACKGROUND(Transformation::getBackgroundModelMatrix), GUI((textureObject, interpolation) -> Transformation.getGUIModelMatrix(textureObject));

    private final ModelMatrixFunction modelMatrixFunction;

    public FloatBuffer get(TextureObject textureObject, float interpolation) {
        return modelMatrixFunction.getMatrixBuffer(textureObject, interpolation);
    }

    private interface ModelMatrixFunction {
        FloatBuffer getMatrixBuffer(TextureObject textureObject, float interpolation);
    }
}
