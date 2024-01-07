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
    private PlayerInputController playerInputController;
    private long lastSendTime;

    @Override
    public void init() {
        core = Core.get();
        guiManager = core.getGuiManager();
        playerInputController = core.getInputHandler().getPlayerInputController();
        core.subscribe(this);
    }

    @Override
    public void update() {
        if (core.isInWorld()) {
            if (!guiManager.isActive()) {
                if (ClientSettings.CAMERA_MOVE_BY_SCREEN_BORDERS.getBoolean()) moveByScreenBorders();

                boolean noShip = !playerInputController.isControllingShip();
                float keyMoveSpeed = ClientSettings.CAMERA_MOVE_BY_KEY_SPEED.getFloat() * Engine.convertToDeltaTime(60.0f);
                if (keyboard.isKeyDown(KEY_LEFT) || (noShip && keyboard.isKeyDown(KEY_A))) {
                    camera.move(-keyMoveSpeed, 0);
                } else if (keyboard.isKeyDown(KEY_RIGHT) || (noShip && keyboard.isKeyDown(KEY_D))) {
                    camera.move(keyMoveSpeed, 0);
                }

                if (keyboard.isKeyDown(KEY_UP) || (noShip && keyboard.isKeyDown(KEY_W))) {
                    camera.move(0, -keyMoveSpeed);
                } else if (keyboard.isKeyDown(KEY_DOWN) || (noShip && keyboard.isKeyDown(KEY_S))) {
                    camera.move(0, keyMoveSpeed);
                }
            }

            if (ClientSettings.CAMERA_FOLLOW_PLAYER.getBoolean()) followShip();

            Vector2f position = camera.getPosition();
            Vector2f lastPosition = camera.getLastPosition();
            if (position.x != lastPosition.x || position.y != lastPosition.y) {
                long time = System.currentTimeMillis();
                if (time - lastSendTime > 500) {
                    core.sendUDPPacket(new PacketCameraPosition(position.x, position.y));
                    lastSendTime = time;
                }
            }
        }
    }

    private void followShip() {
        Vector2f position = camera.getPosition();
        Ship playerShip = playerInputController.getShip();
        if (playerShip != null) {
            Vector2f shipPosition = playerShip.getPosition();
            float minDistance = 0.04f;
            float dis = shipPosition.distanceSquared(position);
            if (dis > minDistance) {
                float mDx = shipPosition.x - position.x;
                float mDy = shipPosition.y - position.y;
                float animationSpeed = Engine.convertToDeltaTime(3.0f);
                camera.move(mDx * animationSpeed, mDy * animationSpeed);
            }
        } else {
            if (followShip == null || followShip.isDead()) {
                findShipToFollow();
            } else {
                Vector2f shipPosition = followShip.getPosition();
                float dis = shipPosition.distanceSquared(position);
                float minDistance = 0.04f;
                if (dis > minDistance) {
                    float mDx = shipPosition.x - position.x;
                    float mDy = shipPosition.y - position.y;
                    float max = 400.0f;
                    if (mDx < -max) mDx = -max;
                    else if (mDx > max) mDx = max;
                    if (mDy < -max) mDy = -max;
                    else if (mDy > max) mDy = max;

                    float animationSpeed = Engine.convertToDeltaTime(3.0f);
                    camera.move(mDx * animationSpeed, mDy * animationSpeed);
                }
            }
        }
    }

    private void findShipToFollow() {
        Ship newShip = null;
        List<Ship> ships = core.getWorld().getEntitiesByType(Ship.class);
        if (ships.size() > 0) {
            float minDist = Float.MAX_VALUE;
            for (int i = 0; i < ships.size(); i++) {
                Ship s = ships.get(i);
                float dist = s.getPosition().distance(camera.getPosition());
                if (dist < minDist) {
                    newShip = s;
                    minDist = dist;
                }
            }

            followShip = newShip;
        }
    }

    private void moveByScreenBorders() {
        float moveSpeed = Engine.convertToDeltaTime(60.0f);
        float screenMoveSpeed = ClientSettings.CAMERA_MOVE_BY_SCREEN_BORDERS_SPEED.getFloat() / camera.getZoom() * moveSpeed;
        float offset = ClientSettings.CAMERA_MOVE_BY_SCREEN_BORDERS_OFFSET.getFloat();
        Vector2f cursorPosition = mouse.getPosition();
        if (cursorPosition.x <= offset) {
            camera.move(-screenMoveSpeed, 0);
        } else if (cursorPosition.x >= renderer.getScreenWidth() - offset) {
            camera.move(screenMoveSpeed, 0);
        }

        if (cursorPosition.y <= offset) {
            camera.move(0, -screenMoveSpeed);
        } else if (cursorPosition.y >= renderer.getScreenHeight() - offset) {
            camera.move(0, screenMoveSpeed);
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