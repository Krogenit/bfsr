package net.bfsr.client.input;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.gui.hud.HUDAdapter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.entity.*;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.network.packet.input.*;
import org.dyn4j.dynamics.Body;
import org.joml.Vector2f;

import java.util.List;

import static net.bfsr.engine.input.Keys.*;

public class PlayerInputController extends InputController {
    private static final int NOT_CONTROLLED_SHIP_ID = -1;

    @Setter
    private int controlledShipId = NOT_CONTROLLED_SHIP_ID;
    @Getter
    private Ship ship;
    private Core core;
    private GuiManager guiManager;
    private final AbstractCamera camera = Engine.renderer.camera;
    private final Vector2f lastMousePosition = new Vector2f();
    private boolean mouseLeftDown;
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private EventBus eventBus;
    private final PositionHistory positionHistory = new PositionHistory(500);

    @Override
    public void init() {
        core = Core.get();
        guiManager = core.getGuiManager();
        eventBus = core.getEventBus();
        eventBus.register(this);
    }

    @Override
    public void update() {
        if (guiManager.isActive()) return;

        if (ship == null) {
            if (controlledShipId == NOT_CONTROLLED_SHIP_ID) {
                return;
            }

            RigidBody<?> entity = core.getWorld().getEntityById(controlledShipId);
            if (!(entity instanceof Ship ship)) {
                return;
            }

            setShip(ship);
        }

        if (ship.isDead()) {
            resetControlledShip();
            return;
        }

        if (!core.isPaused() && ship.isSpawned() && ship.getLifeTime() == 0) {
            controlShip();
        }
    }

    @Override
    public boolean input(int key) {
        if (ship == null || guiManager.isActive()) return false;

        Engines engines = ship.getModules().getEngines();
        if (key == KEY_W && engines.isEngineAlive(Direction.FORWARD)) {
            core.sendUDPPacket(new PacketShipMove(Direction.FORWARD));
            ship.addMoveDirection(Direction.FORWARD);
        }

        if (key == KEY_S && engines.isEngineAlive(Direction.BACKWARD)) {
            core.sendUDPPacket(new PacketShipMove(Direction.BACKWARD));
            ship.addMoveDirection(Direction.BACKWARD);
        }

        if (key == KEY_A && engines.isEngineAlive(Direction.LEFT)) {
            core.sendUDPPacket(new PacketShipMove(Direction.LEFT));
            ship.addMoveDirection(Direction.LEFT);
        }

        if (key == KEY_D && engines.isEngineAlive(Direction.RIGHT)) {
            core.sendUDPPacket(new PacketShipMove(Direction.RIGHT));
            ship.addMoveDirection(Direction.RIGHT);
        }

        if (key == KEY_X && engines.isSomeEngineAlive()) {
            core.sendUDPPacket(new PacketShipMove(Direction.STOP));
            ship.addMoveDirection(Direction.STOP);
        }

        return false;
    }

    @Override
    public void release(int key) {
        if (ship == null || guiManager.isActive()) return;

        if (key == KEY_W) {
            core.sendUDPPacket(new PacketShipStopMove(Direction.FORWARD));
            ship.removeMoveDirection(Direction.FORWARD);
        }

        if (key == KEY_S) {
            core.sendUDPPacket(new PacketShipStopMove(Direction.BACKWARD));
            ship.removeMoveDirection(Direction.BACKWARD);
        }

        if (key == KEY_A) {
            core.sendUDPPacket(new PacketShipStopMove(Direction.LEFT));
            ship.removeMoveDirection(Direction.LEFT);
        }

        if (key == KEY_D) {
            core.sendUDPPacket(new PacketShipStopMove(Direction.RIGHT));
            ship.removeMoveDirection(Direction.RIGHT);
        }

        if (key == KEY_X) {
            core.sendUDPPacket(new PacketShipStopMove(Direction.STOP));
            ship.removeMoveDirection(Direction.STOP);
        }
    }

    private void controlShip() {
        Body body = ship.getBody();
        if (body.isAtRest()) body.setAtRest(false);

        Vector2f mouseWorldPosition = Engine.mouse.getWorldPosition(camera);
        rigidBodyUtils.rotateToVector(ship, mouseWorldPosition, ship.getModules().getEngines().getAngularVelocity());
        if (mouseWorldPosition.x != lastMousePosition.x || mouseWorldPosition.y != lastMousePosition.y) {
            core.sendUDPPacket(new PacketSyncPlayerMousePosition(mouseWorldPosition));
        }

        ship.getMoveDirections().forEach(ship::move);

        if (mouseLeftDown) {
            ship.shoot(weaponSlot -> weaponSlot.createBullet(0));
        }
    }

    @Override
    public boolean onMouseLeftClick() {
        if (guiManager.isActive()) return false;

        if (ship == null) {
            HUDAdapter guiInGame = guiManager.getHud();
            guiInGame.selectShip(null);
            Vector2f mousePosition = Engine.mouse.getWorldPosition(camera);
            List<Ship> ships = core.getWorld().getEntitiesByType(Ship.class);
            for (int i = 0, size = ships.size(); i < size; i++) {
                Ship ship = ships.get(i);
                if (isMouseIntersectsWith(ship, mousePosition.x, mousePosition.y)) {
                    guiInGame.selectShip(ship);
                    return true;
                }
            }
        } else {
            core.sendUDPPacket(new PacketMouseLeftClick());
            mouseLeftDown = true;
        }

        return false;
    }

    @Override
    public boolean onMouseLeftRelease() {
        if (guiManager.isActive() || ship == null) return false;

        core.sendUDPPacket(new PacketMouseLeftRelease());
        mouseLeftDown = false;
        return false;
    }

    @Override
    public boolean onMouseRightClick() {
        if (guiManager.isActive()) return false;

        HUDAdapter guiInGame = guiManager.getHud();
        guiInGame.selectShipSecondary(null);
        Vector2f mousePosition = Engine.mouse.getWorldPosition(camera);
        List<Ship> ships = core.getWorld().getEntitiesByType(Ship.class);
        for (int i = 0, size = ships.size(); i < size; i++) {
            Ship ship = ships.get(i);
            if (isMouseIntersectsWith(ship, mousePosition.x, mousePosition.y)) {
                guiInGame.selectShipSecondary(ship);
                return true;
            }
        }

        return false;
    }

    private boolean isMouseIntersectsWith(GameObject gameObject, float mouseX, float mouseY) {
        Vector2f position = gameObject.getPosition();
        Vector2f size = gameObject.getSize();
        float halfWidth = size.x / 2;
        float halfHeight = size.y / 2;
        return mouseX >= position.x - halfWidth && mouseY >= position.y - halfHeight && mouseX < position.x + halfWidth &&
                mouseY < position.y + halfHeight;
    }

    public void setShip(Ship ship) {
        if (this.ship != null) {
            this.ship.resetPositionCalculatorAndChronologicalProcessor();
            this.ship.setControlledByPlayer(false);
        }

        this.ship = ship;
        guiManager.getHud().selectShip(ship);
        guiManager.getHud().onShipControlStarted();

        if (ship != null) {
            EntityDataHistoryManager historyManager = ship.getWorld().getEntityManager().getDataHistoryManager();
            ship.setPositionCalculator(timestamp -> {
                Vector2f position = ship.getPosition();
                double time = timestamp + Core.get().getClientRenderDelayInNanos();
                positionHistory.addPositionData(position, ship.getSin(), ship.getCos(), time);

                TransformData serverTransformData = historyManager.getTransformData(ship.getId(), timestamp);

                if (serverTransformData == null) {
                    return;
                }

                TransformData localTransformData = positionHistory.get(timestamp);
                if (localTransformData != null) {
                    Vector2f serverPosition = serverTransformData.getPosition();
                    Vector2f localPosition = localTransformData.getPosition();
                    float dx = serverPosition.x - localPosition.x;
                    float dy = serverPosition.y - localPosition.y;
                    ship.setPosition(position.x + dx, position.y + dy);
                    positionHistory.correction(dx, dy);
                }
            });
            ship.setChronologicalDataProcessor(timestamp -> {});
            ship.setControlledByPlayer(true);
        }
    }

    public boolean isControllingShip() {
        return ship != null;
    }

    public void resetControlledShip() {
        setShip(null);
        controlledShipId = NOT_CONTROLLED_SHIP_ID;
        positionHistory.clear();
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> resetControlledShip();
    }
}