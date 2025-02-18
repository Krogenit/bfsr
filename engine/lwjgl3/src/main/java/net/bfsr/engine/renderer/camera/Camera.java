package net.bfsr.engine.renderer.camera;

import lombok.Getter;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.util.MatrixBufferUtils;
import org.jbox2d.collision.AABB;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static net.bfsr.engine.renderer.Renderer.UBO_PROJECTION_MATRIX;
import static net.bfsr.engine.renderer.Renderer.UBO_VIEW_DATA;

public class Camera implements AbstractCamera {
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

    private AbstractRenderer renderer;

    @Override
    public void init(int width, int height, AbstractRenderer renderer) {
        this.width = width;
        this.height = height;
        this.renderer = renderer;

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

        viewBuffer = MemoryUtil.memAllocFloat(5);
        viewBufferAddress = MemoryUtil.memAddress(viewBuffer);
        renderer.putValue(viewBufferAddress, 0);
        renderer.putValue(viewBufferAddress + 4, 0);
        renderer.putValue(viewBufferAddress + 8, zoom);
        renderer.putValue(viewBufferAddress + 12, width);
        renderer.putValue(viewBufferAddress + 16, height);

        GL45C.nglNamedBufferStorage(viewUBO, (long) viewBuffer.capacity() << 2, viewBufferAddress, GL44.GL_DYNAMIC_STORAGE_BIT);

        guiViewBuffer = MemoryUtil.memAllocFloat(5);
        guiViewBufferAddress = MemoryUtil.memAddress(guiViewBuffer);
        renderer.putValue(guiViewBufferAddress, 0);
        renderer.putValue(guiViewBufferAddress + 4, 0);
        renderer.putValue(guiViewBufferAddress + 8, 1.0f);
        renderer.putValue(guiViewBufferAddress + 12, width);
        renderer.putValue(guiViewBufferAddress + 16, height);

        GL45C.nglNamedBufferStorage(guiViewUBO, (long) guiViewBuffer.capacity() << 2, guiViewBufferAddress, GL44.GL_DYNAMIC_STORAGE_BIT);
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
            updateBoundingBox();
        }

        movingAccumulator.set(0, 0);
        zoomAccumulator = 0;
    }

    private void updateBoundingBox() {
        setBoundingBox(position.x + origin.x / zoom, position.y + origin.y / zoom, position.x - origin.x / zoom,
                position.y - origin.y / zoom);
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

        updateBoundingBox();

        renderer.putValue(viewBufferAddress + 12L, width);
        renderer.putValue(viewBufferAddress + 16L, height);
        GL45C.nglNamedBufferSubData(viewUBO, 12L, 8L, viewBufferAddress + 12L);

        renderer.putValue(guiViewBufferAddress + 12L, width);
        renderer.putValue(guiViewBufferAddress + 16L, height);
        GL45C.nglNamedBufferSubData(guiViewUBO, 12L, 8L, guiViewBufferAddress + 12L);

        GL45C.glNamedBufferSubData(GUIProjectionMatrixUBO, 0, orthographicMatrix.setOrtho(0.0f, width, 0.0f, height, Z_NEAR, Z_FAR)
                .get(MatrixBufferUtils.MATRIX_BUFFER));
        GL45C.glNamedBufferSubData(worldProjectionMatrixUBO, 0, orthographicMatrix.setOrtho(-width / 2.0f, width / 2.0f,
                -height / 2.0f, height / 2.0f, Z_NEAR, Z_FAR).get(MatrixBufferUtils.MATRIX_BUFFER));
    }

    @Override
    public Vector2f getWorldVector(Vector2i position) {
        return getWorldVector(position.x, position.y);
    }

    @Override
    public Vector2f getWorldVector(float x, float y) {
        vectorInCamSpace.x = (x + origin.x) / zoom + position.x;
        vectorInCamSpace.y = (y + origin.y) / zoom + position.y;
        return vectorInCamSpace;
    }

    @Override
    public void calculateInterpolatedViewMatrix(float interpolation) {
        float interpolatedPositionX = lastPosition.x + (position.x - lastPosition.x) * interpolation;
        float interpolatedPositionY = lastPosition.y + (position.y - lastPosition.y) * interpolation;
        float interpolatedZoom = lastZoom + (zoom - lastZoom) * interpolation;

        renderer.putValue(viewBufferAddress, -interpolatedPositionX);
        renderer.putValue(viewBufferAddress + 4, -interpolatedPositionY);
        renderer.putValue(viewBufferAddress + 8, interpolatedZoom);

        GL45C.nglNamedBufferSubData(viewUBO, 0, 12L, viewBufferAddress);
    }

    @Override
    public void setBoundingBox(float minX, float minY, float maxX, float maxY) {
        boundingBox.set(minX, minY, maxX, maxY);
    }

    @Override
    public void setPosition(float x, float y) {
        position.set(x, y);
        updateBoundingBox();
    }

    @Override
    public void clear() {
        GL15C.glDeleteBuffers(worldProjectionMatrixUBO);
        GL15C.glDeleteBuffers(GUIProjectionMatrixUBO);
        GL15C.glDeleteBuffers(viewUBO);
        GL15C.glDeleteBuffers(guiViewUBO);

        MemoryUtil.memFree(viewBuffer);
        MemoryUtil.memFree(guiViewBuffer);
    }
}