package net.bfsr.client.camera;

import lombok.Getter;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.MatrixUtils;
import net.bfsr.network.packet.client.PacketCameraPosition;
import net.bfsr.settings.EnumOption;
import net.bfsr.util.MatrixBufferUtils;
import net.bfsr.util.TimeUtils;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.*;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    public static final int UBO_CAMERA_MATRIX = 0;

    private static final float Z_NEAR = -1.0f;
    private static final float Z_FAR = 1.0f;

    private final Matrix4f orthographicMatrix = new Matrix4f();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionViewMatrix = new Matrix4f();
    @Getter
    private final AxisAlignedBoundingBox boundingBox = new AxisAlignedBoundingBox();

    @Getter
    private final Vector2f position = new Vector2f();
    private final Vector2f mouseMovingAccumulator = new Vector2f();
    @Getter
    private final Vector2f lastPosition = new Vector2f();
    private final Vector2f positionAndOrigin = new Vector2f();
    private final Vector2f origin = new Vector2f();
    @Getter
    private float zoom = 10.0f;
    @Getter
    private float lastZoom = zoom;
    private float zoomAccumulator;

    private int width, height;
    private final Vector2f vectorInCamSpace = new Vector2f();
    private long lastSendTime;
    private Ship followShip;

    private int projectionMatrixUBO;
    private int projectionViewMatrixUBO;

    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        origin.set(-width / 2.0f, -height / 2.0f);
        boundingBox.set(position.x + origin.x, position.y + origin.y, position.x - origin.x, position.y - origin.y);

        projectionMatrixUBO = GL45.glCreateBuffers();
        projectionViewMatrixUBO = GL45.glCreateBuffers();

        orthographicMatrix.setOrtho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR);
        GL45.glNamedBufferStorage(projectionMatrixUBO, orthographicMatrix.get(MatrixBufferUtils.MATRIX_BUFFER), GL44.GL_DYNAMIC_STORAGE_BIT);

        MatrixUtils.translateIdentity(viewMatrix, -origin.x, -origin.y);
        MatrixUtils.scale(viewMatrix, zoom, zoom);
        GL45.glNamedBufferStorage(projectionViewMatrixUBO, orthographicMatrix.mul(viewMatrix, projectionViewMatrix).get(MatrixBufferUtils.MATRIX_BUFFER), GL44.GL_DYNAMIC_STORAGE_BIT);
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
        Ship playerShip = Core.get().getWorld().getPlayerShip();
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
        if (Core.get().getWorld().getShips().size() > 0) {
            float minDist = Float.MAX_VALUE;
            List<Ship> ships = Core.get().getWorld().getShips();
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
        if (Core.get().getCurrentGui() == null) {
            zoomAccumulator += y * EnumOption.CAMERA_ZOOM_SPEED.getFloat() * (zoom + zoomAccumulator);
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

        updateZoom();

        position.x += mouseMovingAccumulator.x;
        position.y += mouseMovingAccumulator.y;
        mouseMovingAccumulator.set(0, 0);

        if (Core.get().getWorld() != null) {
            if (Core.get().canControlShip()) {
                if (EnumOption.CAMERA_MOVE_BY_SCREEN_BORDERS.getBoolean()) moveByScreenBorders();
            }

            boolean noShip = Core.get().getWorld().getPlayerShip() == null;
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

            if (position.x != lastPosition.x || position.y != lastPosition.y || lastZoom != zoom) {
                boundingBox.setMinX(position.x + origin.x / zoom);
                boundingBox.setMinY(position.y + origin.y / zoom);
                boundingBox.setMaxX(position.x - origin.x / zoom);
                boundingBox.setMaxY(position.y - origin.y / zoom);

                long time = System.currentTimeMillis();
                if (time - lastSendTime > 500) {
                    Core.get().sendPacket(new PacketCameraPosition(position.x, position.y));
                    lastSendTime = time;
                }
            }
        }
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

    public void bind() {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, UBO_CAMERA_MATRIX, projectionViewMatrixUBO);
    }

    public void bindGUI() {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, UBO_CAMERA_MATRIX, projectionMatrixUBO);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        origin.x = -width / 2.0f;
        origin.y = -height / 2.0f;

        GL45.glNamedBufferSubData(projectionMatrixUBO, 0, orthographicMatrix.setOrtho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR).get(MatrixBufferUtils.MATRIX_BUFFER));
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

    public void onExitToMainMenu() {
        followShip = null;
    }

    public void calculateInterpolatedViewMatrix(float interpolation) {
        float cameraX = lastPosition.x + (position.x - lastPosition.x) * interpolation;
        float cameraY = lastPosition.y + (position.y - lastPosition.y) * interpolation;
        float interpolatedZoom = lastZoom + (zoom - lastZoom) * interpolation;

        MatrixUtils.translateIdentity(viewMatrix.identity(), -origin.x, -origin.y);
        MatrixUtils.scale(viewMatrix, interpolatedZoom, interpolatedZoom);
        MatrixUtils.translate(viewMatrix, -cameraX, -cameraY);
        GL45.glNamedBufferSubData(projectionViewMatrixUBO, 0, orthographicMatrix.mul(viewMatrix, projectionViewMatrix).get(MatrixBufferUtils.MATRIX_BUFFER));
    }
}
