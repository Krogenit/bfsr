package net.bfsr.client.camera;

import lombok.Getter;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.client.PacketCameraPosition;
import net.bfsr.settings.EnumOption;
import net.bfsr.util.TimeUtils;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private static final int PROJECTION_MATRIX_UBO = 0;
    public static final int VIEW_MATRIX_UBO = 1;

    private static final float Z_NEAR = -1.0f;
    private static final float Z_FAR = 100.0f;

    @Getter
    private final Matrix4f orthographicMatrix = new Matrix4f();
    @Getter
    private final Matrix4f viewMatrix = new Matrix4f();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);//можно вынести в отедльный класс
    @Getter
    private final AxisAlignedBoundingBox boundingBox = new AxisAlignedBoundingBox();

    @Getter
    private final Vector2f position = new Vector2f(0, 0);
    private final Vector2f mouseMovingAccumulator = new Vector2f();
    @Getter
    private final Vector2f lastPosition = new Vector2f();
    private final Vector2f positionAndOrigin = new Vector2f();
    @Getter
    private final Vector2f origin = new Vector2f();
    @Getter
    private float zoom = 10.0f;

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

    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        origin.set(-width / 2.0f, -height / 2.0f);
        boundingBox.set(position.x + origin.x, position.y + origin.y, position.x - origin.x, position.y - origin.y);

        projectionMatrixUBO = GL45.glCreateBuffers();
        viewMatrixUBO = GL45.glCreateBuffers();
        GUIViewMatrixUBO = GL45.glCreateBuffers();

        orthographicMatrix.ortho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR);
        orthographicMatrix.get(matrixBuffer);
        GL45.glNamedBufferStorage(projectionMatrixUBO, matrixBuffer, GL44.GL_DYNAMIC_STORAGE_BIT);

        viewMatrix.translate(-origin.x, -origin.y, 0).scale(zoom, zoom, 1.0f).translate(-position.x, -position.y, 0);
        viewMatrix.get(matrixBuffer);
        GL45.glNamedBufferStorage(viewMatrixUBO, matrixBuffer, GL44.GL_DYNAMIC_STORAGE_BIT);

        GL45.glNamedBufferStorage(GUIViewMatrixUBO, new Matrix4f().get(ShaderProgram.MATRIX_BUFFER), 0);
    }

    @Deprecated
    public void setupOpenGLMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, Z_NEAR, Z_FAR);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glTranslatef(-origin.x, -origin.y, 0.0f);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-position.x, -position.y, 0.0f);
    }

    private void followShip() {
        Ship playerShip = Core.getCore().getWorld().getPlayerShip();
        if (playerShip != null) {
            Vector2f shipPosition = playerShip.getPosition();
            float minDistance = 0.04f;
            double dis = shipPosition.distanceSquared(position);
            if (dis > minDistance) {
                double mDx = shipPosition.x - position.x;
                double mDy = shipPosition.y - position.y;
                position.x += mDx * 3.0f * TimeUtils.UPDATE_DELTA_TIME;
                position.y += mDy * 3.0f * TimeUtils.UPDATE_DELTA_TIME;
            }
        } else {
            if (followShip == null || followShip.isDead()) {
                findShipToFollow();
            } else {
                Vector2f shipPosition = followShip.getPosition();
                double dis = shipPosition.distanceSquared(position);
                float minDistance = 0.04f;
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
        }
    }

    private void findShipToFollow() {
        Ship newShip = null;
        if (Core.getCore().getWorld().getShips().size() > 0) {
            float minDist = Float.MAX_VALUE;
            List<Ship> ships = Core.getCore().getWorld().getShips();
            for (int i = 0; i < ships.size(); i++) {
                Ship s = ships.get(i);
                float dist = s.getPosition().distance(position.x, position.y);
                if (dist < minDist) {
                    newShip = s;
                    minDist = dist;
                }
            }

            followShip = newShip;
        }
    }

    private void moveByScreenBorders() {
        float screenMoveSpeed = EnumOption.CAMERA_MOVE_BY_SCREEN_BORDERS_SPEED.getFloat() / zoom;
        float offset = EnumOption.CAMERA_MOVE_BY_SCREEN_BORDERS_OFFSET.getFloat();
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
        float zoomMax = 30.0f;
        float zoomMin = 2.0f;
        float step = EnumOption.CAMERA_ZOOM_SPEED.getFloat() * zoom;
        zoom += y * step;

        if (zoom > zoomMax) {
            zoom = zoomMax;
        } else if (zoom < zoomMin) {
            zoom = zoomMin;
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

        if (Core.getCore().getWorld() != null) {
            if (Core.getCore().canControlShip()) {
                if (EnumOption.CAMERA_MOVE_BY_SCREEN_BORDERS.getBoolean()) moveByScreenBorders();
            }

            boolean noShip = Core.getCore().getWorld().getPlayerShip() == null;
            float keyMoveSpeed = EnumOption.CAMERA_MOVE_BY_KEY_SPEED.getFloat();
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

            if (EnumOption.CAMERA_FOLLOW_PLAYER.getBoolean()) followShip();

            if (position.x != lastPosition.x || position.y != lastPosition.y) {
                viewMatrix.identity();
                viewMatrix.translate(-origin.x, -origin.y, 0).scale(zoom, zoom, 1.0f).translate(-position.x, -position.y, 0);
                viewMatrix.get(matrixBuffer);
                GL45.glNamedBufferSubData(viewMatrixUBO, 0, matrixBuffer);

                boundingBox.setMinX(position.x + origin.x / zoom);
                boundingBox.setMinY(position.y + origin.y / zoom);
                boundingBox.setMaxX(position.x - origin.x / zoom);
                boundingBox.setMaxY(position.y - origin.y / zoom);

                long time = System.currentTimeMillis();
                if (time - lastSendTime > 500) {
                    Core.getCore().sendPacket(new PacketCameraPosition(position.x, position.y));
                    lastSendTime = time;
                }
            }
        }
    }

    public void bind() {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, PROJECTION_MATRIX_UBO, projectionMatrixUBO);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        origin.x = -width / 2.0f;
        origin.y = -height / 2.0f;

        GL45.glNamedBufferSubData(projectionMatrixUBO, 0, orthographicMatrix.identity().ortho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR).get(matrixBuffer));
        GL45.glNamedBufferSubData(viewMatrixUBO, 0, viewMatrix.identity().translate(-origin.x, -origin.y, 0).scale(zoom, zoom, 1.0f).translate(-position.x, -position.y, 0).get(matrixBuffer));
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
}
