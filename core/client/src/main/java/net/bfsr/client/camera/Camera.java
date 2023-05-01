package net.bfsr.client.camera;

import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.settings.Option;
import net.bfsr.client.util.MatrixBufferUtils;
import net.bfsr.math.MatrixUtils;
import org.dyn4j.geometry.AABB;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;

import java.nio.FloatBuffer;

public class Camera {
    public static final int UBO_CAMERA_MATRIX = 0;

    private static final float Z_NEAR = -1.0f;
    private static final float Z_FAR = 1.0f;

    private final Matrix4f orthographicMatrix = new Matrix4f();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionViewMatrix = new Matrix4f();
    @Getter
    private final AABB boundingBox = new AABB(0, 0, 0, 0);

    @Getter
    private final Vector2f position = new Vector2f();
    private final Vector2f mouseMovingAccumulator = new Vector2f();
    @Getter
    private final Vector2f lastPosition = new Vector2f();
    private final Vector2f positionAndOrigin = new Vector2f();
    @Getter
    private final Vector2f origin = new Vector2f();
    @Getter
    private float zoom = 10.0f;
    @Getter
    private float lastZoom = zoom;
    private float zoomAccumulator;

    @Getter
    private int width, height;
    private final Vector2f vectorInCamSpace = new Vector2f();

    private int worldProjectionMatrixUBO;
    private int GUIProjectionMatrixUBO;
    private int projectionViewMatrixUBO;
    private int interpolatedProjectionViewMatrixUBO;

    private Core core;
    private GuiManager guiManager;

    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        this.core = core;
        this.guiManager = guiManager;

        origin.set(-width / 2.0f, -height / 2.0f);
        boundingBox.set(position.x + origin.x, position.y + origin.y, position.x - origin.x, position.y - origin.y);

        worldProjectionMatrixUBO = GL45.glCreateBuffers();
        GUIProjectionMatrixUBO = GL45.glCreateBuffers();
        projectionViewMatrixUBO = GL45.glCreateBuffers();
        interpolatedProjectionViewMatrixUBO = GL45.glCreateBuffers();

        GL45.glNamedBufferStorage(GUIProjectionMatrixUBO, orthographicMatrix.setOrtho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR)
                .get(MatrixBufferUtils.MATRIX_BUFFER), GL44.GL_DYNAMIC_STORAGE_BIT);
        GL45.glNamedBufferStorage(worldProjectionMatrixUBO, orthographicMatrix.setOrtho(-width / 2.0f, width / 2.0f, height / 2.0f, -height / 2.0f, Z_NEAR, Z_FAR)
                .get(MatrixBufferUtils.MATRIX_BUFFER), GL44.GL_DYNAMIC_STORAGE_BIT);

        MatrixUtils.scale(viewMatrix, zoom, zoom);
        FloatBuffer matrixBuffer = orthographicMatrix.mul(viewMatrix, projectionViewMatrix).get(MatrixBufferUtils.MATRIX_BUFFER);
        GL45.glNamedBufferStorage(projectionViewMatrixUBO, matrixBuffer, GL44.GL_DYNAMIC_STORAGE_BIT);
        GL45.glNamedBufferStorage(interpolatedProjectionViewMatrixUBO, matrixBuffer, GL44.GL_DYNAMIC_STORAGE_BIT);
    }

    public void zoom(float value) {
        zoomAccumulator += value * Option.CAMERA_ZOOM_SPEED.getFloat() * (zoom + zoomAccumulator);
    }

    public void moveByMouse(float dx, float dy) {
        mouseMovingAccumulator.x -= dx / zoom;
        mouseMovingAccumulator.y -= dy / zoom;
    }

    public void update() {
        lastPosition.set(position.x, position.y);

        updateZoom();

        position.x += mouseMovingAccumulator.x;
        position.y += mouseMovingAccumulator.y;
        mouseMovingAccumulator.set(0, 0);
    }

    private void updateZoom() {
        lastZoom = zoom;

        float zoomMax = 30.0f;
        float zoomMin = 2.0f;

        zoom += zoomAccumulator;

        if (zoom > zoomMax) {
            zoom = zoomMax;
        } else if (zoom < zoomMin) {
            zoom = zoomMin;
        }

        zoomAccumulator = 0;
    }

    public void bindInterpolatedWorldViewMatrix() {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, UBO_CAMERA_MATRIX, interpolatedProjectionViewMatrixUBO);
    }

    public void bindWorldViewMatrix() {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, UBO_CAMERA_MATRIX, projectionViewMatrixUBO);
    }

    public void bindGUI() {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, UBO_CAMERA_MATRIX, GUIProjectionMatrixUBO);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        origin.x = -width / 2.0f;
        origin.y = -height / 2.0f;

        GL45.glNamedBufferSubData(GUIProjectionMatrixUBO, 0, orthographicMatrix.setOrtho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR).get(MatrixBufferUtils.MATRIX_BUFFER));
        GL45.glNamedBufferSubData(worldProjectionMatrixUBO, 0, orthographicMatrix.setOrtho(-width / 2.0f, width / 2.0f, height / 2.0f, -height / 2.0f, Z_NEAR, Z_FAR)
                .get(MatrixBufferUtils.MATRIX_BUFFER));
    }

    public Vector2f getWorldVector(Vector2f pos) {
        return getWorldVector(pos.x, pos.y);
    }

    public Vector2f getWorldVector(float x, float y) {
        vectorInCamSpace.x = (x + origin.x) / zoom + position.x;
        vectorInCamSpace.y = (y + origin.y) / zoom + position.y;
        return vectorInCamSpace;
    }

    public Vector2f getPositionAndOrigin() {
        positionAndOrigin.x = position.x + origin.x;
        positionAndOrigin.y = position.y + origin.y;
        return positionAndOrigin;
    }

    public void calculateInterpolatedViewMatrix(float interpolation) {
        float cameraX = lastPosition.x + (position.x - lastPosition.x) * interpolation;
        float cameraY = lastPosition.y + (position.y - lastPosition.y) * interpolation;
        float interpolatedZoom = lastZoom + (zoom - lastZoom) * interpolation;

        MatrixUtils.scale(viewMatrix.identity(), interpolatedZoom, interpolatedZoom);
        MatrixUtils.translate(viewMatrix, -cameraX, -cameraY);
        GL45.glNamedBufferSubData(interpolatedProjectionViewMatrixUBO, 0, orthographicMatrix.mul(viewMatrix, projectionViewMatrix).get(MatrixBufferUtils.MATRIX_BUFFER));

        MatrixUtils.scale(viewMatrix.identity(), zoom, zoom);
        MatrixUtils.translate(viewMatrix, -position.x, -position.y);
        GL45.glNamedBufferSubData(projectionViewMatrixUBO, 0, orthographicMatrix.mul(viewMatrix, projectionViewMatrix).get(MatrixBufferUtils.MATRIX_BUFFER));
    }

    public void setBoundingBox(float minX, float minY, float maxX, float maxY) {
        boundingBox.set(minX, minY, maxX, maxY);
    }
}