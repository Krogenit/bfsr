package net.bfsr.client.input;

import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.client.world.entity.PlayerShipManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.entity.ship.Ship;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.List;

import static net.bfsr.engine.input.Keys.KEY_DOWN;
import static net.bfsr.engine.input.Keys.KEY_LEFT;
import static net.bfsr.engine.input.Keys.KEY_RIGHT;
import static net.bfsr.engine.input.Keys.KEY_UP;

public class CameraInputController extends InputController {
    private static final float ZOOM_MAX = 300.0f;
    private static final float ZOOM_MIN = 20.0f;

    private final Client client;
    private final AbstractRenderer renderer = Engine.getRenderer();
    private final AbstractCamera camera = renderer.getCamera();
    private final GuiManager guiManager = Engine.getGuiManager();
    private final AbstractMouse mouse = Engine.getMouse();
    private final AbstractKeyboard keyboard = Engine.getKeyboard();
    private final PlayerShipManager playerShipManager;

    private @Nullable Ship followShip;

    @Getter
    private float zoom;
    @Getter
    private float normalizedZoom = 0.5f;

    private final Vector2f movingAccumulator = new Vector2f();
    private float zoomAccumulator;

    public CameraInputController(Client client, PlayerShipManager playerShipManager) {
        this.client = client;
        this.playerShipManager = playerShipManager;
        client.getEventBus().register(this);
        updateZoom();
        camera.setZoom(zoom);
    }

    @Override
    public void update(int frame) {
        if (!client.isInWorld()) {
            return;
        }

        if (!guiManager.isActive()) {
            if (ClientSettings.CAMERA_MOVE_BY_SCREEN_BORDERS.getBoolean()) {
                moveByScreenBorders();
            }

            float keyMoveSpeed = ClientSettings.CAMERA_MOVE_BY_KEY_SPEED.getFloat() * Engine.convertToDeltaTime(60.0f);
            if (keyboard.isKeyDown(KEY_LEFT)) {
                movingAccumulator.add(-keyMoveSpeed, 0);
            } else if (keyboard.isKeyDown(KEY_RIGHT)) {
                movingAccumulator.add(keyMoveSpeed, 0);
            }

            if (keyboard.isKeyDown(KEY_UP)) {
                movingAccumulator.add(0, keyMoveSpeed);
            } else if (keyboard.isKeyDown(KEY_DOWN)) {
                movingAccumulator.add(0, -keyMoveSpeed);
            }
        }

        if (ClientSettings.CAMERA_FOLLOW_PLAYER.getBoolean()) {
            followShip();
        }

        if (zoomAccumulator != 0) {
            updateZoom();
            camera.setZoom(zoom);
        }

        if (movingAccumulator.x != 0 || movingAccumulator.y != 0) {
            camera.move(movingAccumulator.x, movingAccumulator.y);
        }

        movingAccumulator.set(0, 0);
        zoomAccumulator = 0;
    }

    private void followShip() {
        Vector2f position = camera.getPosition();
        Ship playerShip = playerShipManager.getShip();
        if (playerShip != null) {
            float x = playerShip.getX();
            float y = playerShip.getY();
            float minDistance = 0.00002f;
            float dis = position.distanceSquared(x, y);
            if (dis > minDistance) {
                float mDx = x - position.x;
                float mDy = y - position.y;
                float animationSpeed = Engine.convertToDeltaTime(3.0f);
                movingAccumulator.add(mDx * animationSpeed, mDy * animationSpeed);
            }
        } else {
            if (followShip == null || followShip.isDead() || !followShip.getModules().getEngines().isSomeEngineAlive()) {
                findShipToFollow();
            } else {
                float x = followShip.getX();
                float y = followShip.getY();
                float dis = position.distanceSquared(x, y);
                float minDistance = 0.04f;
                if (dis > minDistance) {
                    float mDx = x - position.x;
                    float mDy = y - position.y;
                    float max = 400.0f;
                    if (mDx < -max) mDx = -max;
                    else if (mDx > max) mDx = max;
                    if (mDy < -max) mDy = -max;
                    else if (mDy > max) mDy = max;

                    float animationSpeed = Engine.convertToDeltaTime(3.0f);
                    movingAccumulator.add(mDx * animationSpeed, mDy * animationSpeed);
                }
            }
        }
    }

    private void findShipToFollow() {
        Ship newShip = null;
        List<Ship> ships = client.getWorld().getEntitiesByType(Ship.class);
        if (ships.size() > 0) {
            float minDist = Float.MAX_VALUE;
            for (int i = 0; i < ships.size(); i++) {
                Ship ship = ships.get(i);
                if (ship.getModules().getEngines().isSomeEngineAlive()) {
                    float dist = camera.getPosition().distance(ship.getX(), ship.getY());
                    if (dist < minDist) {
                        newShip = ship;
                        minDist = dist;
                    }
                }
            }

            followShip = newShip;
        }
    }

    private void moveByScreenBorders() {
        float moveSpeed = Engine.convertToDeltaTime(60.0f);
        float screenMoveSpeed = ClientSettings.CAMERA_MOVE_BY_SCREEN_BORDERS_SPEED.getFloat() / camera.getZoom() * moveSpeed;
        float offset = ClientSettings.CAMERA_MOVE_BY_SCREEN_BORDERS_OFFSET.getFloat();
        Vector2f cursorPosition = mouse.getScreenPosition();
        if (cursorPosition.x <= offset) {
            movingAccumulator.add(-screenMoveSpeed, 0);
        } else if (cursorPosition.x >= renderer.getScreenWidth() - offset) {
            movingAccumulator.add(screenMoveSpeed, 0);
        }

        if (cursorPosition.y <= offset) {
            movingAccumulator.add(0, screenMoveSpeed);
        } else if (cursorPosition.y >= renderer.getScreenHeight() - offset) {
            movingAccumulator.add(0, -screenMoveSpeed);
        }
    }

    private void updateZoom() {
        normalizedZoom += zoomAccumulator;
        if (normalizedZoom > 1) {
            normalizedZoom = 1.0f;
        } else if (normalizedZoom < 0) {
            normalizedZoom = 0;
        }

        zoom = ZOOM_MIN + zoomFunction(normalizedZoom) * (ZOOM_MAX - ZOOM_MIN);
    }

    private float zoomFunction(float x) {
        return x < 0.5 ? 8 * x * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 4) / 2;
    }

    @Override
    public boolean scroll(float scrollY) {
        zoomAccumulator += (scrollY * ClientSettings.CAMERA_ZOOM_SPEED.getFloat());
        return true;
    }

    @Override
    public boolean mouseMove(float x, float y) {
        if (mouse.isRightDown()) {
            moveCamera(-x / zoom, y / zoom);
            return true;
        }

        return false;
    }

    private void moveCamera(float x, float y) {
        movingAccumulator.x += x;
        movingAccumulator.y += y;
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> followShip = null;
    }
}