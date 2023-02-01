package net.bfsr.math;

import net.bfsr.client.camera.Camera;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.nio.FloatBuffer;

public class ModelMatrixUtils {
    private static final Matrix4f viewMatrixGui = new Matrix4f();
    private static final Matrix4f modelViewMatrix = new Matrix4f();
    private static final Matrix4f finalModelViewMatrix = new Matrix4f();
    private static final Matrix4f matrix = new Matrix4f();

    public static Matrix4f getModelViewMatrixGui(float x, float y, float rotation, float scaleX, float scaleY) {
        modelViewMatrix.identity().translate(x, y, 0);
        if (rotation != 0) modelViewMatrix.rotateZ(rotation);
        modelViewMatrix.scale(scaleX, scaleY, 1);
        return viewMatrixGui.mul(modelViewMatrix, finalModelViewMatrix);
    }

    public static FloatBuffer getModelMatrixBuffer(TextureObject textureObject, float interpolation) {
        return textureObject.getModelMatrixType().getMatrixBuffer(textureObject, interpolation);
    }

    public static Matrix4f getModelMatrix(TextureObject textureObject, float interpolation) {
        return textureObject.getModelMatrixType().getMatrix(textureObject, interpolation);
    }

    public static Matrix4f getDefaultModelMatrix(float prevX, float prevY, float x, float y, float rotation, float scaleX, float scaleY, float interpolation) {
        return getDefaultModelMatrix(prevX, prevY, x, y, rotation, scaleX, scaleY, interpolation, matrix);
    }

    public static Matrix4f getDefaultModelMatrix(float prevX, float prevY, float x, float y, float rotation, float scaleX, float scaleY, float interpolation, Matrix4f destMatrix) {
        Camera camera = Core.getCore().getRenderer().getCamera();

        float cameraX = camera.getLastPosition().x + (camera.getPosition().x - camera.getLastPosition().x) * interpolation;
        float cameraY = camera.getLastPosition().y + (camera.getPosition().y - camera.getLastPosition().y) * interpolation;

        destMatrix.identity().translate(-camera.getOrigin().x, -camera.getOrigin().y, 0).scale(camera.getZoom(), camera.getZoom(), 1.0f)
                .translate(-cameraX + prevX + (x - prevX) * interpolation, -cameraY + prevY + (y - prevY) * interpolation, 0);
        if (rotation != 0) destMatrix.rotateZ(rotation);
        return destMatrix.scale(scaleX, scaleY, 1.0f);
    }

    public static FloatBuffer getDefaultModelMatrixBuffer(TextureObject textureObject, float interpolation) {
        return getDefaultModelMatrix(textureObject, interpolation).get(ShaderProgram.MATRIX_BUFFER);
    }

    public static Matrix4f getDefaultModelMatrix(TextureObject textureObject, float interpolation) {
        return getDefaultModelMatrix(textureObject, interpolation, matrix);
    }

    public static Matrix4f getDefaultModelMatrix(TextureObject textureObject, float interpolation, Matrix4f destMatrix) {
        return getDefaultModelMatrix(textureObject.getLastPosition().x, textureObject.getLastPosition().y, textureObject.getPosition().x, textureObject.getPosition().y,
                textureObject.getRotation(), textureObject.getScale().x, textureObject.getScale().y, interpolation, destMatrix);
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

        return matrix.identity().translate(-camOrigin.x, -camOrigin.y, 0).scale(zoom, zoom, 1.0f).translate(-x * moveFactor + textureObject.getPosition().x,
                -y * moveFactor + textureObject.getPosition().y, 0.0f).scale(textureObject.getScale().x, textureObject.getScale().y, 1.0f);
    }

    public static FloatBuffer getGUIModelMatrixBuffer(TextureObject textureObject) {
        return getGUIModelMatrix(textureObject).get(ShaderProgram.MATRIX_BUFFER);
    }

    public static Matrix4f getGUIModelMatrix(TextureObject textureObject) {
        matrix.identity().translate(textureObject.getPosition().x, textureObject.getPosition().y, 0.0f);
        if (textureObject.getRotation() != 0) matrix.rotateZ(textureObject.getRotation());
        return matrix.scale(textureObject.getScale().x, textureObject.getScale().y, 1.0f);
    }

    public static Matrix4f getGUIModelMatrix(float x, float y, float rotation, float scaleX, float scaleY) {
        return matrix.identity().translate(x, y, 0.0f).rotateZ(rotation).scale(scaleX, scaleY, 1.0f);
    }
}
