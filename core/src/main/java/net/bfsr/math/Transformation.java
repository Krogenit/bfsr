package net.bfsr.math;

import net.bfsr.client.camera.Camera;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.nio.FloatBuffer;

public class Transformation {
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

    public static FloatBuffer getModelMatrix(TextureObject textureObject, float interpolation) {
        return textureObject.getModelMatrixType().get(textureObject, interpolation);
    }

    public static FloatBuffer getDefaultModelMatrix(float prevX, float prevY, float x, float y, float rotation, float scaleX, float scaleY, float interpolation) {
        Camera camera = Core.getCore().getRenderer().getCamera();

        float cameraX = camera.getLastPosition().x + (camera.getPosition().x - camera.getLastPosition().x) * interpolation;
        float cameraY = camera.getLastPosition().y + (camera.getPosition().y - camera.getLastPosition().y) * interpolation;

        matrix.identity().translate(-camera.getOrigin().x, -camera.getOrigin().y, 0).scale(camera.getZoom(), camera.getZoom(), 1.0f)
                .translate(-cameraX + prevX + (x - prevX) * interpolation, -cameraY + prevY + (y - prevY) * interpolation, 0);
        if (rotation != 0) matrix.rotateZ(rotation);
        matrix.scale(scaleX, scaleY, 1.0f);
        return matrix.get(ShaderProgram.MATRIX_BUFFER);
    }

    public static FloatBuffer getDefaultModelMatrix(TextureObject textureObject, float interpolation) {
        Camera camera = Core.getCore().getRenderer().getCamera();

        Vector2f lastPosition = textureObject.getLastPosition();
        Vector2f position = textureObject.getPosition();
        float rotation = textureObject.getRotation();
        Vector2f scale = textureObject.getScale();

        float cameraX = camera.getLastPosition().x + (camera.getPosition().x - camera.getLastPosition().x) * interpolation;
        float cameraY = camera.getLastPosition().y + (camera.getPosition().y - camera.getLastPosition().y) * interpolation;

        matrix.identity().translate(-camera.getOrigin().x, -camera.getOrigin().y, 0).scale(camera.getZoom(), camera.getZoom(), 1.0f)
                .translate(-cameraX + lastPosition.x + (position.x - lastPosition.x) * interpolation, -cameraY + lastPosition.y + (position.y - lastPosition.y) * interpolation, 0);
        if (rotation != 0) matrix.rotateZ(rotation);
        matrix.scale(scale.x, scale.y, 1);
        return matrix.get(ShaderProgram.MATRIX_BUFFER);
    }

    public static FloatBuffer getBackgroundModelMatrix(TextureObject textureObject, float interpolation) {
        Camera camera = Core.getCore().getRenderer().getCamera();

        Vector2f camPos = camera.getPosition();
        Vector2f lastPosition = camera.getLastPosition();
        Vector2f camOrigin = camera.getOrigin();

        float x = lastPosition.x + (camPos.x - lastPosition.x) * interpolation;
        float y = lastPosition.y + (camPos.y - lastPosition.y) * interpolation;
        float moveFactor = 0.005f;
        float zoom = 0.5f + camera.getZoom() * 0.001f;

        matrix.identity().translate(-camOrigin.x, -camOrigin.y, 0).scale(zoom, zoom, 1.0f)
                .translate(-x * moveFactor + textureObject.getPosition().x, -y * moveFactor + textureObject.getPosition().y, 0.0f).scale(textureObject.getScale().x, textureObject.getScale().y, 1.0f);
        return matrix.get(ShaderProgram.MATRIX_BUFFER);
    }

    public static FloatBuffer getGUIModelMatrix(TextureObject textureObject) {
        return matrix.identity().translate(textureObject.getPosition().x, textureObject.getPosition().y, 0.0f).scale(textureObject.getScale().x, textureObject.getScale().y, 1.0f)
                .get(ShaderProgram.MATRIX_BUFFER);
    }

    public static FloatBuffer getGUIModelMatrix(float x, float y, float rotation, float scaleX, float scaleY) {
        return matrix.identity().translate(x, y, 0.0f).rotateZ(rotation).scale(scaleX, scaleY, 1.0f).get(ShaderProgram.MATRIX_BUFFER);
    }
}
