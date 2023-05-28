package net.bfsr.client.input;

import net.bfsr.client.Core;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.util.TimeUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.client.PacketCameraPosition;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import org.joml.Vector2f;

import java.util.List;

import static net.bfsr.engine.input.Keys.*;

@Listener
public class CameraInputController extends InputController {
    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractCamera camera = renderer.camera;
    private GuiManager guiManager;
    private final AbstractMouse mouse = Engine.mouse;
    private final AbstractKeyboard keyboard = Engine.keyboard;
    private Core core;
    private Ship followShip;
    private Vector2f position;
    private PlayerInputController playerInputController;
    private long lastSendTime;

    @Override
    public void init() {
        core = Core.get();
        guiManager = core.getGuiManager();
        playerInputController = core.getInputHandler().getPlayerInputController();
        position = camera.getPosition();
        core.subscribe(this);
    }

    @Override
    public void update() {
        if (core.isInWorld()) {
            if (!guiManager.isActive()) {
                if (ClientSettings.CAMERA_MOVE_BY_SCREEN_BORDERS.getBoolean()) moveByScreenBorders();

                boolean noShip = !playerInputController.isControllingShip();
                float keyMoveSpeed = ClientSettings.CAMERA_MOVE_BY_KEY_SPEED.getFloat() * 60.0f * TimeUtils.UPDATE_DELTA_TIME;
                if (keyboard.isKeyDown(KEY_LEFT) || (noShip && keyboard.isKeyDown(KEY_A))) {
                    position.x -= keyMoveSpeed;
                } else if (keyboard.isKeyDown(KEY_RIGHT) || (noShip && keyboard.isKeyDown(KEY_D))) {
                    position.x += keyMoveSpeed;
                }

                if (keyboard.isKeyDown(KEY_UP) || (noShip && keyboard.isKeyDown(KEY_W))) {
                    position.y -= keyMoveSpeed;
                } else if (keyboard.isKeyDown(KEY_DOWN) || (noShip && keyboard.isKeyDown(KEY_S))) {
                    position.y += keyMoveSpeed;
                }
            }

            if (ClientSettings.CAMERA_FOLLOW_PLAYER.getBoolean()) followShip();

            Vector2f lastPosition = camera.getLastPosition();
            float zoom = camera.getZoom();
            float lastZoom = camera.getLastZoom();
            if (position.x != lastPosition.x || position.y != lastPosition.y || lastZoom != zoom) {
                Vector2f origin = camera.getOrigin();
                camera.setBoundingBox(position.x + origin.x / zoom, position.y + origin.y / zoom, position.x - origin.x / zoom,
                        position.y - origin.y / zoom);

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
        float screenMoveSpeed = ClientSettings.CAMERA_MOVE_BY_SCREEN_BORDERS_SPEED.getFloat() / camera.getZoom() * moveSpeed;
        float offset = ClientSettings.CAMERA_MOVE_BY_SCREEN_BORDERS_OFFSET.getFloat();
        Vector2f cursorPosition = mouse.getPosition();
        if (cursorPosition.x <= offset) {
            position.x -= screenMoveSpeed;
        } else if (cursorPosition.x >= renderer.getScreenWidth() - offset) {
            position.x += screenMoveSpeed;
        }

        if (cursorPosition.y <= offset) {
            position.y -= screenMoveSpeed;
        } else if (cursorPosition.y >= renderer.getScreenHeight() - offset) {
            position.y += screenMoveSpeed;
        }
    }

    @Override
    public boolean scroll(float y) {
        if (guiManager.noGui() || guiManager.getGui().isAllowCameraZoom()) {
            camera.zoom(y * ClientSettings.CAMERA_ZOOM_SPEED.getFloat());
            return true;
        }

        return false;
    }

    @Override
    public void mouseMove(float x, float y) {
        if (mouse.isRightDown()) {
            camera.moveByMouse(x, y);
        }
    }

    @Handler
    public void event(ExitToMainMenuEvent event) {
        followShip = null;
    }
}