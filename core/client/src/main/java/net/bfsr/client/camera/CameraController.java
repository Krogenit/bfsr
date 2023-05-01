package net.bfsr.client.camera;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.input.PlayerInputController;
import net.bfsr.client.network.packet.client.PacketCameraPosition;
import net.bfsr.client.settings.Option;
import net.bfsr.entity.ship.Ship;
import net.bfsr.util.TimeUtils;
import org.joml.Vector2f;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController {
    private Core core;
    private PlayerInputController playerInputController;
    private GuiManager guiManager;
    private Camera camera;
    private long lastSendTime;
    private Ship followShip;
    private Vector2f position;

    public void init() {
        core = Core.get();
        playerInputController = core.getInputHandler().getPlayerInputController();
        guiManager = core.getGuiManager();
        camera = core.getRenderer().getCamera();
        position = camera.getPosition();
    }

    public void update() {
        if (core.getWorld() != null) {
            if (!guiManager.isActive()) {
                if (Option.CAMERA_MOVE_BY_SCREEN_BORDERS.getBoolean()) moveByScreenBorders();
            }

            boolean noShip = !playerInputController.isControllingShip();
            float keyMoveSpeed = Option.CAMERA_MOVE_BY_KEY_SPEED.getFloat() * 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            boolean noScreen = guiManager.getCurrentGui() == null;
            if (noScreen) {
                if (Keyboard.isKeyDown(GLFW_KEY_LEFT) || (noShip && Keyboard.isKeyDown(GLFW_KEY_A))) {
                    position.x -= keyMoveSpeed;
                } else if (Keyboard.isKeyDown(GLFW_KEY_RIGHT) || (noShip && Keyboard.isKeyDown(GLFW_KEY_D))) {
                    position.x += keyMoveSpeed;
                }

                if (Keyboard.isKeyDown(GLFW_KEY_UP) || (noShip && Keyboard.isKeyDown(GLFW_KEY_W))) {
                    position.y -= keyMoveSpeed;
                } else if (Keyboard.isKeyDown(GLFW_KEY_DOWN) || (noShip && Keyboard.isKeyDown(GLFW_KEY_S))) {
                    position.y += keyMoveSpeed;
                }
            }

            if (Option.CAMERA_FOLLOW_PLAYER.getBoolean()) followShip();

            Vector2f lastPosition = camera.getLastPosition();
            float zoom = camera.getZoom();
            float lastZoom = camera.getLastZoom();
            if (position.x != lastPosition.x || position.y != lastPosition.y || lastZoom != zoom) {
                Vector2f origin = camera.getOrigin();
                camera.setBoundingBox(position.x + origin.x / zoom, position.y + origin.y / zoom, position.x - origin.x / zoom, position.y - origin.y / zoom);

                long time = System.currentTimeMillis();
                if (time - lastSendTime > 500) {
                    core.sendUDPPacket(new PacketCameraPosition(position.x, position.y));
                    lastSendTime = time;
                }
            }
        }
    }

    private void followShip() {
        Ship playerShip = playerInputController.getShip();
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
        if (core.getWorld().getShips().size() > 0) {
            float minDist = Float.MAX_VALUE;
            List<Ship> ships = core.getWorld().getShips();
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
        float moveSpeed = 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        float screenMoveSpeed = Option.CAMERA_MOVE_BY_SCREEN_BORDERS_SPEED.getFloat() / camera.getZoom() * moveSpeed;
        float offset = Option.CAMERA_MOVE_BY_SCREEN_BORDERS_OFFSET.getFloat();
        Vector2f cursorPosition = Mouse.getPosition();
        if (cursorPosition.x <= offset) {
            position.x -= screenMoveSpeed;
        } else if (cursorPosition.x >= camera.getWidth() - offset) {
            position.x += screenMoveSpeed;
        }

        if (cursorPosition.y <= offset) {
            position.y -= screenMoveSpeed;
        } else if (cursorPosition.y >= camera.getHeight() - offset) {
            position.y += screenMoveSpeed;
        }
    }

    public void onExitToMainMenu() {
        followShip = null;
    }
}