package net.bfsr.engine.renderer.camera;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.util.MatrixBufferUtils;
import org.jbox2d.collision.AABB;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL45C;

import java.nio.FloatBuffer;

import static net.bfsr.engine.renderer.Renderer.UBO_PROJECTION_MATRIX;
import static net.bfsr.engine.renderer.Renderer.UBO_VIEW_DATA;

public class Camera extends AbstractCamera {
    private static final float Z_NEAR = -1.0f;
    private static final float Z_FAR = 1.0f;
    private static final float ZOOM_MAX = 30.0f;
    private static final float ZOOM_MIN = 2.0f;

    private final Matrix4f orthographicMatrix = new Matrix4f();
    @Getter
    private final AABB boundingBox = new AABB();

    @Getter
    private final Vector2f position = new Vector2f();
    private final Vector2f movingAccumulator = new Vector2f();
    @Getter
    private final Vector2f lastPosition = new Vector2f();
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

    private FloatBuffer viewBuffer;
    private long viewBufferAddress;
    private int viewUBO;

    private FloatBuffer guiViewBuffer;
    private long guiViewBufferAddress;
    private int guiViewUBO;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        origin.set(-width / 2.0f, -height / 2.0f);
        boundingBox.set(position.x + origin.x, position.y + origin.y, position.x - origin.x, position.y - origin.y);

        worldProjectionMatrixUBO = GL45.glCreateBuffers();
        GUIProjectionMatrixUBO = GL45.glCreateBuffers();
        viewUBO = GL45.glCreateBuffers();
        guiViewUBO = GL45.glCreateBuffers();

        GL45.glNamedBufferStorage(GUIProjectionMatrixUBO, orthographicMatrix.setOrtho(0.0f, width, 0.0f, height, Z_NEAR, Z_FAR)
                .get(MatrixBufferUtils.MATRIX_BUFFER), GL44.GL_DYNAMIC_STORAGE_BIT);
        GL45.glNamedBufferStorage(worldProjectionMatrixUBO, orthographicMatrix.setOrtho(-width / 2.0f, width / 2.0f,
                        -height / 2.0f, height / 2.0f, Z_NEAR, Z_FAR)
                .get(MatrixBufferUtils.MATRIX_BUFFER), GL44.GL_DYNAMIC_STORAGE_BIT);

        viewBuffer = Engine.renderer.createFloatBuffer(3);
        viewBufferAddress = Engine.renderer.getAddress(viewBuffer);
        Engine.renderer.putValue(viewBufferAddress, 0);
        Engine.renderer.putValue(viewBufferAddress + 4, 0);
        Engine.renderer.putValue(viewBufferAddress + 8, zoom);

        GL45C.nglNamedBufferStorage(viewUBO, 12L, viewBufferAddress, GL44.GL_DYNAMIC_STORAGE_BIT);

        guiViewBuffer = Engine.renderer.createFloatBuffer(3);
        guiViewBufferAddress = Engine.renderer.getAddress(guiViewBuffer);
        Engine.renderer.putValue(guiViewBufferAddress, 0);
        Engine.renderer.putValue(guiViewBufferAddress + 4, 0);
        Engine.renderer.putValue(guiViewBufferAddress + 8, 1.0f);

        GL45C.nglNamedBufferStorage(guiViewUBO, 12L, guiViewBufferAddress, GL44.GL_DYNAMIC_STORAGE_BIT);
    }

    @Override
    public void zoom(float value) {
        zoomAccumulator += value * zoom;
    }

    @Override
    public void move(float x, float y) {
        movingAccumulator.x += x;
        movingAccumulator.y += y;
    }

    @Override
    public void moveByMouse(float dx, float dy) {
        movingAccumulator.x -= dx / zoom;
        movingAccumulator.y -= dy / zoom;
    }

    @Override
    public void update() {
        lastPosition.set(position.x, position.y);

        updateZoom();

        position.x += movingAccumulator.x;
        position.y += movingAccumulator.y;

        if (movingAccumulator.x != 0 || movingAccumulator.y != 0 || zoomAccumulator != 0) {
            setBoundingBox(position.x + origin.x / zoom, position.y + origin.y / zoom, position.x - origin.x / zoom,
                    position.y - origin.y / zoom);
        }

        movingAccumulator.set(0, 0);
        zoomAccumulator = 0;
    }

    private void updateZoom() {
        lastZoom = zoom;

        zoom += zoomAccumulator;

        if (zoom > ZOOM_MAX) {
            zoom = ZOOM_MAX;
        } else if (zoom < ZOOM_MIN) {
            zoom = ZOOM_MIN;
        }
    }

    @Override
    public void bindWorldViewMatrix() {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, UBO_PROJECTION_MATRIX, worldProjectionMatrixUBO);
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, UBO_VIEW_DATA, viewUBO);
    }

    @Override
    public void bindGUI() {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, UBO_PROJECTION_MATRIX, GUIProjectionMatrixUBO);
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, UBO_VIEW_DATA, guiViewUBO);
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        origin.x = -width / 2.0f;
        origin.y = -height / 2.0f;

        GL45.glNamedBufferSubData(GUIProjectionMatrixUBO, 0, orthographicMatrix.setOrtho(0.0f, width, 0.0f, height, Z_NEAR, Z_FAR)
                .get(MatrixBufferUtils.MATRIX_BUFFER));
        GL45.glNamedBufferSubData(worldProjectionMatrixUBO, 0, orthographicMatrix.setOrtho(-width / 2.0f, width / 2.0f,
                -height / 2.0f, height / 2.0f, Z_NEAR, Z_FAR).get(MatrixBufferUtils.MATRIX_BUFFER));
    }

    @Override
    public Vector2f getWorldVector(Vector2f position) {
        return getWorldVector(position.x, position.y);
    }

    public Vector2f getWorldVector(float x, float y) {
        vectorInCamSpace.x = (x + origin.x) / zoom + position.x;
        vectorInCamSpace.y = (-y - origin.y) / zoom + position.y;
        return vectorInCamSpace;
    }

    @Override
    public void calculateInterpolatedViewMatrix(float interpolation) {
        float interpolatedPositionX = lastPosition.x + (position.x - lastPosition.x) * interpolation;
        float interpolatedPositionY = lastPosition.y + (position.y - lastPosition.y) * interpolation;
        float interpolatedZoom = lastZoom + (zoom - lastZoom) * interpolation;

        Engine.renderer.putValue(viewBufferAddress, -interpolatedPositionX);
        Engine.renderer.putValue(viewBufferAddress + 4, -interpolatedPositionY);
        Engine.renderer.putValue(viewBufferAddress + 8, interpolatedZoom);

        GL45C.nglNamedBufferSubData(viewUBO, 0, 12L, viewBufferAddress);
    }

    @Override
    public void setBoundingBox(float minX, float minY, float maxX, float maxY) {
        boundingBox.set(minX, minY, maxX, maxY);
    }
}