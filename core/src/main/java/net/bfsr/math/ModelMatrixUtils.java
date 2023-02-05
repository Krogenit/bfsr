package net.bfsr.math;

import org.joml.Matrix4f;

@Deprecated
public class ModelMatrixUtils {
    @Deprecated
    private static final Matrix4f MODEL_VIEW_MATRIX = new Matrix4f();

    @Deprecated
    public static Matrix4f getModelViewMatrixGui(float x, float y, float rotation, float scaleX, float scaleY) {
        MatrixUtils.translateIdentity(MODEL_VIEW_MATRIX.identity(), x, y);
        if (rotation != 0) MatrixUtils.rotateAboutZ(MODEL_VIEW_MATRIX, rotation);
        MatrixUtils.scale(MODEL_VIEW_MATRIX, scaleX, scaleY);
        return MODEL_VIEW_MATRIX;
    }
}
