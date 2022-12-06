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
import net.bfsr.world.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private final Core core;
    private final ClientSettings settings;
    private static final float Z_NEAR = 0.0f;
    private static final float Z_FAR = 100.0f;
    @Getter
    private final Matrix4f orthographicMatrix;
    @Getter
    private final Matrix4f viewMatrix;
    private final FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);
    @Getter
    private final AxisAlignedBoundingBox boundingBox;

    @Getter
    private final Vector2f position;
    private final Vector2f positionAndOrigin;
    @Getter
    private float rotation;
    @Getter
    private final Vector2f origin;
    @Getter
    private float zoom, zoomBackground;

    private int width, height;
    private final Vector2f vectorInCamSpace = new Vector2f();
    private long lastSendTime;
    private Ship followShip;

    public Camera(int width, int height) {
        orthographicMatrix = new Matrix4f().ortho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR);
        orthographicMatrix.get(projectionMatrixBuffer);

        position = new Vector2f(0, 0);
        rotation = 0.0f;
        origin = new Vector2f(-width / 2.0f, -height / 2.0f);
        this.width = width;
        this.height = height;
        zoom = zoomBackground = 1.0f;
        positionAndOrigin = new Vector2f();
        core = Core.getCore();
        settings = core.getSettings();
        boundingBox = new AxisAlignedBoundingBox(new Vector2f(position.x + origin.x, position.y + origin.y), new Vector2f(position.x - origin.x, position.y - origin.y));
        viewMatrix = new Matrix4f().translate(-origin.x, -origin.y, 0).scale(zoom, zoom, 1.0f).translate(-position.x, -position.y, 0);
        viewMatrix.get(viewMatrixBuffer);
    }

    private void followPlayer(double delta) {
        Ship playerShip = core.getWorld().getPlayerShip();
        if (playerShip != null) {
            Vector2f shipPosition = playerShip.getPosition();
            float minDistance = 2.0f;
            double dis = shipPosition.distance(position);
            if (dis > minDistance) {
                double mDx = shipPosition.x - position.x;
                double mDy = shipPosition.y - position.y;
                position.x += mDx * 3.0f * delta;
                position.y += mDy * 3.0f * delta;
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
                float minDistance = 2.0f;
                if (dis > minDistance) {
                    double mDx = shipPosition.x - position.x;
                    double mDy = shipPosition.y - position.y;
                    float max = 400.0f;
                    if (mDx < -max) mDx = -max;
                    else if (mDx > max) mDx = max;
                    if (mDy < -max) mDy = -max;
                    else if (mDy > max) mDy = max;

                    position.x += mDx * 3.0f * delta;
                    position.y += mDy * 3.0f * delta;
                }
            }
//			}
        }
    }

    private void moveByScreenBorders(double delta) {
        float screenMoveSpeed = settings.getCameraMoveByScreenBordersSpeed() / zoom;
        float offset = settings.getCameraMoveByScreenBordersOffset();
        float moveSpeed = 60.0f;
        Vector2f cursorPosition = Mouse.getPosition();
        if (cursorPosition.x <= offset) {
            position.x -= screenMoveSpeed * moveSpeed * delta;
        } else if (cursorPosition.x >= width - offset) {
            position.x += screenMoveSpeed * moveSpeed * delta;
        }

        if (cursorPosition.y <= offset) {
            position.y -= screenMoveSpeed * moveSpeed * delta;
        } else if (cursorPosition.y >= height - offset) {
            position.y += screenMoveSpeed * moveSpeed * delta;
        }
    }

    public void scroll(float y) {
        float zoomMax = 2.5f;
        float zoomMin = 0.3f;
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

    public void update(double delta) {
        if (core.getWorld() != null) {
            if (core.canControlShip()) {
                if (settings.isCameraMoveByScreenBorders()) moveByScreenBorders(delta);
            }

            boolean noShip = core.getWorld().getPlayerShip() == null;
            float keyMoveSpeed = settings.getCameraMoveByKeySpeed();
            if (Keyboard.isKeyDown(GLFW_KEY_LEFT) || (noShip && Keyboard.isKeyDown(GLFW_KEY_A))) {
                position.x -= keyMoveSpeed * 60.0f * delta;
            } else if (Keyboard.isKeyDown(GLFW_KEY_RIGHT) || (noShip && Keyboard.isKeyDown(GLFW_KEY_D))) {
                position.x += keyMoveSpeed * 60.0f * delta;
            }

            if (Keyboard.isKeyDown(GLFW_KEY_UP) || (noShip && Keyboard.isKeyDown(GLFW_KEY_W))) {
                position.y -= keyMoveSpeed * 60.0f * delta;
            } else if (Keyboard.isKeyDown(GLFW_KEY_DOWN) || (noShip && Keyboard.isKeyDown(GLFW_KEY_S))) {
                position.y += keyMoveSpeed * 60.0f * delta;
            }

            if (Mouse.isRightDown()) {
                Vector2f delta1 = Mouse.getDelta();
                position.x -= delta1.x / zoom * 60.0f * delta;
                position.y -= delta1.y / zoom * 60.0f * delta;
            }

            if (settings.isCameraFollowPlayer()) followPlayer(delta);
        } else {
            zoom = zoomBackground = 1.0f;
            position.x = 0;
            position.y = 0;
        }

        viewMatrix.identity();
        viewMatrix.translate(-origin.x, -origin.y, 0);
        viewMatrix.scale(zoom, zoom, 1.0f);
        viewMatrix.translate(-position.x, -position.y, 0);
        viewMatrix.get(viewMatrixBuffer);

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

    public void resize(int width, int height) {
        orthographicMatrix.identity();
        orthographicMatrix.ortho(0.0f, width, height, 0.0f, Z_NEAR, Z_FAR);
        orthographicMatrix.get(projectionMatrixBuffer);
        Transformation.resize(width, height);
        this.width = width;
        this.height = height;
        origin.x = -width / 2.0f;
        origin.y = -height / 2.0f;
    }

    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
    }

    public void setRotation(float x) {
        rotation = x;
    }

    public void rotate(float offsetX) {
        rotation += offsetX;
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

    public boolean isIntersects(Vector2f pos) {
        return boundingBox.isIntersects(pos);
    }

    public boolean isIntersects(AxisAlignedBoundingBox aabb1) {
        return boundingBox.isIntersects(aabb1);
    }
}
