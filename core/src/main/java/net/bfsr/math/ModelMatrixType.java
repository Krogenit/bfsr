package net.bfsr.math;

import lombok.AllArgsConstructor;
import net.bfsr.entity.TextureObject;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

@AllArgsConstructor
public enum ModelMatrixType {
    DEFAULT(ModelMatrixUtils::getDefaultModelMatrixBuffer, ModelMatrixUtils::getDefaultModelMatrix, ModelMatrixUtils::getDefaultModelMatrix),
    BACKGROUND(ModelMatrixUtils::getBackgroundModelMatrixBuffer, ModelMatrixUtils::getBackgroundModelMatrix, null),
    GUI((textureObject, interpolation) -> ModelMatrixUtils.getGUIModelMatrixBuffer(textureObject), (textureObject, interpolation) -> ModelMatrixUtils.getGUIModelMatrix(textureObject),
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
