package net.bfsr.math;

import net.bfsr.client.camera.Camera;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.nio.FloatBuffer;

public class Transformation {
    private static final Matrix4f viewMatrix = new Matrix4f();
    private static final Matrix4f viewMatrixBackground = new Matrix4f();
    private static final Matrix4f viewMatrixGui = new Matrix4f();
    private static final Matrix4f modelViewMatrix = new Matrix4f();
    private static final Matrix4f finalModelViewMatrix = new Matrix4f();
    private static final Matrix4f matrix = new Matrix4f();

    public static final Vector2f guiScale = new Vector2f(Core.getCore().getWidth() / 1280.0f, Core.getCore().getHeight() / 720.0f);

    public static Matrix4f getModelViewMatrix(float x, float y, float rotation, float scaleX, float scaleY, EnumZoomFactor factor) {
        modelViewMatrix.identity().translate(x, y, 0);
        if (rotation != 0) modelViewMatrix.rotateZ(rotation);
        modelViewMatrix.scale(scaleX, -scaleY, 1);

        Matrix4f viewMatrixByType = getViewMatrixByType(factor);
        return viewMatrixByType.mul(modelViewMatrix, finalModelViewMatrix);
    }

    public static Matrix4f getModelViewMatrixGui(float x, float y, float rotation, float scaleX, float scaleY) {
        modelViewMatrix.identity().translate(x, y, 0);
        if (rotation != 0) modelViewMatrix.rotateZ(rotation);
        modelViewMatrix.scale(scaleX, scaleY, 1);
        return viewMatrixGui.mul(modelViewMatrix, finalModelViewMatrix);
    }

    public static Matrix4f getModelViewMatrix(TextureObject gameObj) {
        return getModelViewMatrix(gameObj, 1.0f);
    }

    public static Matrix4f getModelViewMatrix(TextureObject gameObj, float interpolation) {
        Vector2f lastPosition = gameObj.getLastPosition();
        Vector2f position = gameObj.getPosition();
        float rotation = gameObj.getRotation();
        Vector2f scale = gameObj.getScale();
        EnumZoomFactor zoomFactor = gameObj.getZoomFactor();

        modelViewMatrix.identity().translate(lastPosition.x + (position.x - lastPosition.x) * interpolation, lastPosition.y + (position.y - lastPosition.y) * interpolation, 0);
        if (rotation != 0) modelViewMatrix.rotateZ(rotation);
        modelViewMatrix.scale(scale.x, scale.y, 1);

        Matrix4f viewMatrixByType = getViewMatrixByType(zoomFactor);
        return viewMatrixByType.mul(modelViewMatrix, finalModelViewMatrix);
    }

    public static void updateViewMatrix(Camera camera, float interpolation) {
        Vector2f camPos = camera.getPosition();
        Vector2f lastPosition = camera.getLastPosition();
        Vector2f camOrigin = camera.getOrigin();
        float zoom = camera.getZoom();

        viewMatrix.identity();
        viewMatrix.translate(-camOrigin.x, -camOrigin.y, 0);
        viewMatrix.scale(zoom, zoom, 1);
        float x = lastPosition.x + (camPos.x - lastPosition.x) * interpolation;
        float y = lastPosition.y + (camPos.y - lastPosition.y) * interpolation;
        viewMatrix.translate(-x, -y, 0);
    }

    public static FloatBuffer getModelMatrix(TextureObject textureObject, float interpolation) {
        return textureObject.getModelMatrixType().get(textureObject, interpolation);
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

    public static Matrix4f getViewMatrixByType(EnumZoomFactor zoomFactor) {
        return zoomFactor.getMatrix();
    }

    public static Matrix4f getBackgroundViewMatrix() {
        return viewMatrixBackground;
    }

    public static Matrix4f getDefaultViewMatrix() {
        return viewMatrix;
    }

    public static Matrix4f getGuiViewMatrix() {
        return viewMatrixGui;
    }

    public static void resize(int width, int height) {
        guiScale.x = width / 1280f;
        guiScale.y = height / 720f;
    }

    public static Vector2f getScale(float x, float y) {
        return new Vector2f(x * guiScale.x, y * guiScale.y);
    }

    public static Vector2f getScale(Vector2f scale) {
        return new Vector2f(scale.x * guiScale.x, scale.y * guiScale.y);
    }

    public static Vector2f getOffsetByScale(float x, float y) {
        return new Vector2f(x - (x - Core.getCore().getWidth() / 2f) * (1.0f - guiScale.x),
                y - (y - Core.getCore().getHeight() / 2f) * (1.0f - guiScale.y));
    }

    public static Vector2f getOffsetByScale(Vector2f pos) {
        return new Vector2f(pos.x - (pos.x - Core.getCore().getWidth() / 2f) * (1.0f - guiScale.x),
                pos.y - (pos.y - Core.getCore().getHeight() / 2f) * (1.0f - guiScale.y));
    }
}
