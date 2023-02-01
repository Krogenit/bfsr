package net.bfsr.math;

import lombok.AllArgsConstructor;
import net.bfsr.entity.TextureObject;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

@AllArgsConstructor
public enum ModelMatrixType {
    DEFAULT(Transformation::getDefaultModelMatrixBuffer, Transformation::getDefaultModelMatrix, Transformation::getDefaultModelMatrix),
    BACKGROUND(Transformation::getBackgroundModelMatrixBuffer, Transformation::getBackgroundModelMatrix, null),
    GUI((textureObject, interpolation) -> Transformation.getGUIModelMatrixBuffer(textureObject), (textureObject, interpolation) -> Transformation.getGUIModelMatrix(textureObject),
            null);

    private final ModelMatrixBufferFunction modelMatrixBufferFunction;
    private final ModelMatrixFunction modelMatrixFunction;
    private final ModelMatrixFunctionWithDestMatrix modelMatrixFunctionWithDestMatrix;

    public FloatBuffer getMatrixBuffer(TextureObject textureObject, float interpolation) {
        return modelMatrixBufferFunction.getMatrixBuffer(textureObject, interpolation);
    }

    public Matrix4f getMatrix(TextureObject textureObject, float interpolation) {
        return modelMatrixFunction.getMatrix(textureObject, interpolation);
    }

    public Matrix4f getMatrix(TextureObject textureObject, float interpolation, Matrix4f destMatrix) {
        return modelMatrixFunctionWithDestMatrix.getMatrix(textureObject, interpolation, destMatrix);
    }

    private interface ModelMatrixBufferFunction {
        FloatBuffer getMatrixBuffer(TextureObject textureObject, float interpolation);
    }

    private interface ModelMatrixFunction {
        Matrix4f getMatrix(TextureObject textureObject, float interpolation);
    }

    private interface ModelMatrixFunctionWithDestMatrix {
        Matrix4f getMatrix(TextureObject textureObject, float interpolation, Matrix4f destMatrix);
    }
}
