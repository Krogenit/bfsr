package net.bfsr.client.camera;

import lombok.Getter;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.client.PacketCameraPosition;
import net.bfsr.settings.ClientSettings;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    public static final int PROJECTION_MATRIX_UBO = 0;
    public static final int VIEW_MATRIX_UBO = 1;

    private static final float Z_NEAR = 0.0f;
    private static final float Z_FAR = 100.0f;

    private final Core core;
    private final ClientSettings settings;
    @Getter
    private final Matrix4f orthographicMatrix;
    @Getter
    private final Matrix4f viewMatrix = new Matrix4f();
    private final FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);
    @Getter
    private final AxisAlignedBoundingBox boundingBox = new AxisAlignedBoundingBox();

    @Getter
    private final Vector2f position;
    private final Vector2f mouseMovingAccumulator = new Vector2f();
    @Getter
    private final Vector2f lastPosition = new Vector2f();
    private final Vector2f positionAndOrigin;
    @Getter
    private float rotation;
    @Getter
    private final Vector2f origin = new Vector2f();
    @Getter
    private float zoom, zoomBackground;

    private int width, height;
    private final Vector2f vectorInCamSpace = new Vector2f();
    private long lastSendTime;
    private Ship followShip;

    @Getter
    private int projectionMatrixUBO;
    @Getter
    private int viewMatrixUBO;
    @Getter
    private int GUIViewMatrixUBO;

    public Camera() {
        orthographicMatrix = new Matrix4f();

        position = new Vector2f(0, 0);
        rotation = 0.0f;
        zoom = 10.0f;
        zoomBackground = 1.0f;
        positionAndOrigin = new Vector2f();
        core = Core.getCore();
        settings = core.getSettings();
    }

    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        orthographicMatrix.ortho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR);
        orthographicMatrix.get(projectionMatrixBuffer);
        origin.set(-width / 2.0f, -height / 2.0f);
        boundingBox.set(position.x + origin.x, position.y + origin.y, position.x - origin.x, position.y - origin.y);
        viewMatrix.translate(-origin.x, -origin.y, 0).scale(zoom, zoom, 1.0f).translate(-position.x, -position.y, 0);
        viewMatrix.get(viewMatrixBuffer);
        projectionMatrixUBO = GL45.glCreateBuffers();
        GL45.glNamedBufferStorage(projectionMatrixUBO, projectionMatrixBuffer, GL44.GL_DYNAMIC_STORAGE_BIT);
        viewMatrixUBO = GL45.glCreateBuffers();
        GL45.glNamedBufferStorage(viewMatrixUBO, 16 * 4, GL44.GL_DYNAMIC_STORAGE_BIT);
        GUIViewMatrixUBO = GL45.glCreateBuffers();
        Matrix4f matrix = new Matrix4f();
        matrix.get(viewMatrixBuffer);
        GL45.glNamedBufferStorage(GUIViewMatrixUBO, viewMatrixBuffer, 0);
    }

    @Deprecated
    public void setupOpenGLMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, Z_NEAR, Z_FAR);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glTranslatef(-position.x - origin.x, -position.y - origin.y, 0);
        GL11.glTranslatef(position.x, position.y, 0);
        GL11.glScalef(zoom, zoom, 1f);
        GL11.glTranslatef(-position.x, -position.y, 0);
    }

    private void followPlayer() {
        Ship playerShip = core.getWorld().getPlayerShip();
        if (playerShip != null) {
            Vector2f shipPosition = playerShip.getPosition();
            float minDistance = 0.2f;
            double dis = shipPosition.distance(position);
            if (dis > minDistance) {
                double mDx = shipPosition.x - position.x;
                double mDy = shipPosition.y - position.y;
                position.x += mDx * 3.0f * TimeUtils.UPDATE_DELTA_TIME;
                position.y += mDy * 3.0f * TimeUtils.UPDATE_DELTA_TIME;
            }
        } else {
//			boolean hasShip = false;
            World world = core.getWorld();
//			List<Ship> ships = world.getShips();
//			for(Ship s : ships) {
//				if(followShip == s) hasShip = true;
//			}
//			if(settings.isDebug()) {
            if (followShip == null || followShip.isDead()
//						|| !hasShip
            ) {
                Ship newShip = null;
                if (world.getShips().size() > 0) {
                    float minDist = Float.MAX_VALUE;
                    for (Ship s : world.getShips()) {
                        float dist = s.getPosition().distance(position.x, position.y);
                        if (dist < minDist) {
                            newShip = s;
                            minDist = dist;
                        }
                    }

                    followShip = newShip;
                }
            } else {
                Vector2f shipPosition = followShip.getPosition();
                double dis = shipPosition.distance(position);
                float minDistance = 0.2f;
                if (dis > minDistance) {
                    double mDx = shipPosition.x - position.x;
                    double mDy = shipPosition.y - position.y;
                    float max = 400.0f;
                    if (mDx < -max) mDx = -max;
                    else if (mDx > max) mDx = max;
                    if (mDy < -max) mDy = -max;
                    else if (mDy > max) mDy = max;

                    position.x += mDx * 3.0f * TimeUtils.UPDATE_DELTA_TIME;
                    position.y += mDy * 3.0f * TimeUtils.UPDATE_DELTA_TIME;
                }
            }
//			}
        }
    }

    private void moveByScreenBorders() {
        float screenMoveSpeed = settings.getCameraMoveByScreenBordersSpeed() / zoom;
        float offset = settings.getCameraMoveByScreenBordersOffset();
        float moveSpeed = 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        Vector2f cursorPosition = Mouse.getPosition();
        if (cursorPosition.x <= offset) {
            position.x -= screenMoveSpeed * moveSpeed;
        } else if (cursorPosition.x >= width - offset) {
            position.x += screenMoveSpeed * moveSpeed;
        }

        if (cursorPosition.y <= offset) {
            position.y -= screenMoveSpeed * moveSpeed;
        } else if (cursorPosition.y >= height - offset) {
            position.y += screenMoveSpeed * moveSpeed;
        }
    }

    public void scroll(float y) {
        float zoomMax = 50.0f;
        float zoomMin = 1.0f;
        float step = settings.getCameraZoomSpeed() * zoom;
        float maxSteps = (zoomMax - zoomMin) / step;
        zoom += y * step;

        if (zoom > zoomMax) {
            zoom = zoomMax;
        } else if (zoom < zoomMin) {
            zoom = zoomMin;
        }

        zoomMax = 1.0075f;
        zoomMin = 0.9925f;
        step = (zoomMax - zoomMin) / maxSteps;

        zoomBackground += y * step;

        if (zoomBackground > zoomMax) {
            zoomBackground = zoomMax;
        } else if (zoomBackground < zoomMin) {
            zoomBackground = zoomMin;
        }
    }

    public void mouseMove(float dx, float dy) {
        if (Mouse.isRightDown()) {
            mouseMovingAccumulator.x -= dx / zoom;
            mouseMovingAccumulator.y -= dy / zoom;
        }
    }

    public void update() {
        lastPosition.set(position.x, position.y);

        position.x += mouseMovingAccumulator.x;
        position.y += mouseMovingAccumulator.y;
        mouseMovingAccumulator.set(0, 0);

        if (core.getWorld() != null) {
            if (core.canControlShip()) {
                if (settings.isCameraMoveByScreenBorders()) moveByScreenBorders();
            }

            boolean noShip = core.getWorld().getPlayerShip() == null;
            float keyMoveSpeed = settings.getCameraMoveByKeySpeed();
            if (Keyboard.isKeyDown(GLFW_KEY_LEFT) || (noShip && Keyboard.isKeyDown(GLFW_KEY_A))) {
                position.x -= keyMoveSpeed * 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            } else if (Keyboard.isKeyDown(GLFW_KEY_RIGHT) || (noShip && Keyboard.isKeyDown(GLFW_KEY_D))) {
                position.x += keyMoveSpeed * 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            }

            if (Keyboard.isKeyDown(GLFW_KEY_UP) || (noShip && Keyboard.isKeyDown(GLFW_KEY_W))) {
                position.y -= keyMoveSpeed * 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            } else if (Keyboard.isKeyDown(GLFW_KEY_DOWN) || (noShip && Keyboard.isKeyDown(GLFW_KEY_S))) {
                position.y += keyMoveSpeed * 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            }

            if (settings.isCameraFollowPlayer()) followPlayer();
        } else {
            zoom = zoomBackground = 1.0f;
            position.x = 0;
            position.y = 0;
        }

        if (position.x != lastPosition.x || position.y != lastPosition.y) {
            viewMatrix.identity();
            viewMatrix.translate(-origin.x, -origin.y, 0);
            viewMatrix.scale(zoom, zoom, 1.0f);
            viewMatrix.translate(-position.x, -position.y, 0);
            viewMatrix.get(viewMatrixBuffer);

            GL45.glNamedBufferSubData(viewMatrixUBO, 0, viewMatrixBuffer);

            boundingBox.setMinX(position.x + origin.x / zoom);
            boundingBox.setMinY(position.y + origin.y / zoom);
            boundingBox.setMaxX(position.x - origin.x / zoom);
            boundingBox.setMaxY(position.y - origin.y / zoom);

            long time = System.currentTimeMillis();
            if (time - lastSendTime > 500) {
                core.sendPacket(new PacketCameraPosition(position.x, position.y));
                lastSendTime = time;
            }
        }
    }

    public void bind() {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, PROJECTION_MATRIX_UBO, projectionMatrixUBO);
    }

    public void resize(int width, int height) {
        orthographicMatrix.identity();
        orthographicMatrix.ortho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR);
        orthographicMatrix.get(projectionMatrixBuffer);
        Transformation.resize(width, height);
        this.width = width;
        this.height = height;
        origin.x = -width / 2.0f;
        origin.y = -height / 2.0f;
        GL45.glNamedBufferSubData(projectionMatrixUBO, 0, projectionMatrixBuffer);

        viewMatrix.identity();
        viewMatrix.translate(-origin.x, -origin.y, 0);
        viewMatrix.scale(zoom, zoom, 1.0f);
        viewMatrix.translate(-position.x, -position.y, 0);
        viewMatrix.get(viewMatrixBuffer);

        GL45.glNamedBufferSubData(viewMatrixUBO, 0, viewMatrixBuffer);
    }

    public Vector2f getWorldVector(Vector2f pos) {
        vectorInCamSpace.x = (pos.x + origin.x) / zoom + position.x;
        vectorInCamSpace.y = (pos.y + origin.y) / zoom + position.y;
        return vectorInCamSpace;
    }

    public Vector2f getPositionAndOrigin() {
        positionAndOrigin.x = position.x + origin.x;
        positionAndOrigin.y = position.y + origin.y;
        return positionAndOrigin;
    }

    public void clear() {
        followShip = null;
    }

    public boolean isIntersects(Vector2f vector) {
        return boundingBox.isIntersects(vector);
    }

    public boolean isIntersects(AxisAlignedBoundingBox aabb) {
        return boundingBox.isIntersects(aabb);
    }
}
