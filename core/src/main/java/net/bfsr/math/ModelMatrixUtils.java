package net.bfsr.math;

import net.bfsr.client.camera.Camera;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.nio.FloatBuffer;

public class ModelMatrixUtils {
    private static final Matrix4f MODEL_VIEW_MATRIX = new Matrix4f();
    private static final Matrix4f MODEL_MATRIX = new Matrix4f();

    public static Matrix4f getModelViewMatrixGui(float x, float y, float rotation, float scaleX, float scaleY) {
        MatrixUtils.translateIdentity(MODEL_VIEW_MATRIX.identity(), x, y);
        if (rotation != 0) MatrixUtils.rotateAboutZ(MODEL_VIEW_MATRIX, rotation);
        MatrixUtils.scale(MODEL_VIEW_MATRIX, scaleX, scaleY);
        return MODEL_VIEW_MATRIX;
    }

    public static FloatBuffer getModelMatrixBuffer(TextureObject textureObject, float interpolation) {
        return textureObject.getModelMatrixType().getMatrixBuffer(textureObject, interpolation);
    }

    public static Matrix4f getModelMatrix(TextureObject textureObject, float interpolation) {
        return textureObject.getModelMatrixType().getMatrix(textureObject, interpolation);
    }

    public static Matrix4f getDefaultModelMatrix(float prevX, float prevY, float x, float y, float lastRotation, float rotation, float scaleX, float scaleY, float interpolation) {
        return getDefaultModelMatrix(prevX, prevY, x, y, lastRotation, rotation, scaleX, scaleY, interpolation, MODEL_MATRIX);
    }

    public static Matrix4f getDefaultModelMatrix(float prevX, float prevY, float x, float y, float lastRotation, float rotation, float oldScaleX, float oldScaleY,
                                                 float scaleX, float scaleY, float interpolation) {
        return getDefaultModelMatrix(prevX, prevY, x, y, lastRotation, rotation, oldScaleX, oldScaleY, scaleX, scaleY, interpolation, MODEL_MATRIX);
    }

    public static Matrix4f getDefaultModelMatrix(float prevX, float prevY, float x, float y, float lastRotation, float rotation, float scaleX, float scaleY, float interpolation,
                                                 Matrix4f destMatrix) {
        destMatrix.set(Core.getCore().getRenderer().getCamera().getInterpolatedViewMatrix());
        MatrixUtils.translate(destMatrix, prevX + (x - prevX) * interpolation, prevY + (y - prevY) * interpolation);
        if (rotation != 0) MatrixUtils.rotateAboutZ(destMatrix, lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation);
        MatrixUtils.scale(destMatrix, scaleX, scaleY);
        return destMatrix;
    }

    public static Matrix4f getDefaultModelMatrix(float prevX, float prevY, float x, float y, float lastRotation, float rotation, float oldScaleX, float oldScaleY,
                                                 float scaleX, float scaleY, float interpolation, Matrix4f destMatrix) {
        destMatrix.set(Core.getCore().getRenderer().getCamera().getInterpolatedViewMatrix());
        MatrixUtils.translate(destMatrix, prevX + (x - prevX) * interpolation, prevY + (y - prevY) * interpolation);
        if (rotation != 0) MatrixUtils.rotateAboutZ(destMatrix, lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation);
        MatrixUtils.scale(destMatrix, oldScaleX + (scaleX - oldScaleX) * interpolation, oldScaleY + (scaleY - oldScaleY) * interpolation);
        return destMatrix;
    }

    public static FloatBuffer getDefaultModelMatrixBuffer(TextureObject textureObject, float interpolation) {
        return getDefaultModelMatrix(textureObject, interpolation).get(ShaderProgram.MATRIX_BUFFER);
    }

    public static Matrix4f getDefaultModelMatrix(TextureObject textureObject, float interpolation) {
        return getDefaultModelMatrix(textureObject, interpolation, MODEL_MATRIX);
    }

    public static Matrix4f getDefaultModelMatrix(TextureObject textureObject, float interpolation, Matrix4f destMatrix) {
        return getDefaultModelMatrix(textureObject.getLastPosition().x, textureObject.getLastPosition().y, textureObject.getPosition().x, textureObject.getPosition().y,
                textureObject.getLastRotation(), textureObject.getRotation(), textureObject.getScale().x, textureObject.getScale().y, interpolation, destMatrix);
    }

    public static FloatBuffer getBackgroundModelMatrixBuffer(TextureObject textureObject, float interpolation) {
        return getBackgroundModelMatrix(textureObject, interpolation).get(ShaderProgram.MATRIX_BUFFER);
    }

    public static Matrix4f getBackgroundModelMatrix(TextureObject textureObject, float interpolation) {
        Camera camera = Core.getCore().getRenderer().getCamera();

        Vector2f camPos = camera.getPosition();
        Vector2f lastPosition = camera.getLastPosition();
        Vector2f camOrigin = camera.getOrigin();

        float x = lastPosition.x + (camPos.x - lastPosition.x) * interpolation;
        float y = lastPosition.y + (camPos.y - lastPosition.y) * interpolation;
        float moveFactor = 0.005f;
        float zoom = 0.5f + camera.getZoom() * 0.001f;
        MatrixUtils.translateIdentity(MODEL_MATRIX.identity(), -camOrigin.x, -camOrigin.y);
        MatrixUtils.scale(MODEL_MATRIX, zoom, zoom);
        MatrixUtils.translate(MODEL_MATRIX, -x * moveFactor + textureObject.getPosition().x, -y * moveFactor + textureObject.getPosition().y);
        MatrixUtils.scale(MODEL_MATRIX, textureObject.getScale().x, textureObject.getScale().y);
        return MODEL_MATRIX;
    }

    public static FloatBuffer getGUIModelMatrixBuffer(TextureObject textureObject) {
        return getGUIModelMatrix(textureObject).get(ShaderProgram.MATRIX_BUFFER);
    }

    public static Matrix4f getGUIModelMatrix(TextureObject textureObject) {
        MatrixUtils.translateIdentity(MODEL_MATRIX.identity(), textureObject.getPosition().x, textureObject.getPosition().y);
        if (textureObject.getRotation() != 0) MatrixUtils.rotateAboutZ(MODEL_MATRIX, textureObject.getRotation());
        MatrixUtils.scale(MODEL_MATRIX, textureObject.getScale().x, textureObject.getScale().y);
        return MODEL_MATRIX;
    }

    public static Matrix4f getGUIModelMatrix(float x, float y, float rotation, float scaleX, float scaleY) {
        MatrixUtils.translateIdentity(MODEL_MATRIX.identity(), x, y);
        if (rotation != 0) MatrixUtils.rotateAboutZ(MODEL_MATRIX, rotation);
        MatrixUtils.scale(MODEL_MATRIX, scaleX, scaleY);
        return MODEL_MATRIX;
    }
}
