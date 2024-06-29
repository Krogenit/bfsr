package net.bfsr.client.input;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.event.gui.SelectSecondaryShipEvent;
import net.bfsr.client.event.gui.SelectShipEvent;
import net.bfsr.client.event.player.ShipControlStartedEvent;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.entity.EntityDataHistoryManager;
import net.bfsr.entity.PositionHistory;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.TransformData;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.network.packet.input.PacketMouseLeftClick;
import net.bfsr.network.packet.input.PacketMouseLeftRelease;
import net.bfsr.network.packet.input.PacketShipMove;
import net.bfsr.network.packet.input.PacketShipStopMove;
import net.bfsr.network.packet.input.PacketSyncPlayerMousePosition;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import org.joml.Vector2f;

import java.util.List;

import static net.bfsr.engine.input.Keys.KEY_A;
import static net.bfsr.engine.input.Keys.KEY_D;
import static net.bfsr.engine.input.Keys.KEY_S;
import static net.bfsr.engine.input.Keys.KEY_W;
import static net.bfsr.engine.input.Keys.KEY_X;

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
    private final PositionHistory positionHistory = new PositionHistory(500);
    private EventBus eventBus;

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

            RigidBody entity = core.getWorld().getEntityById(controlledShipId);
            if (!(entity instanceof Ship ship)) {
                return;
            }

            setShip(ship);
            eventBus.publish(new SelectShipEvent(ship));
            eventBus.publish(new ShipControlStartedEvent());
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
        if (ship == null) return false;

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
            core.sendUDPPacket(new PacketShipMove(Direction.RIGHT));
            ship.addMoveDirection(Direction.RIGHT);
        }

        if (key == KEY_D && engines.isEngineAlive(Direction.RIGHT)) {
            core.sendUDPPacket(new PacketShipMove(Direction.LEFT));
            ship.addMoveDirection(Direction.LEFT);
        }

        if (key == KEY_X && engines.isSomeEngineAlive()) {
            core.sendUDPPacket(new PacketShipMove(Direction.STOP));
            ship.addMoveDirection(Direction.STOP);
        }

        return false;
    }

    @Override
    public void release(int key) {
        if (ship == null) return;

        if (key == KEY_W) {
            core.sendUDPPacket(new PacketShipStopMove(Direction.FORWARD));
            ship.removeMoveDirection(Direction.FORWARD);
        }

        if (key == KEY_S) {
            core.sendUDPPacket(new PacketShipStopMove(Direction.BACKWARD));
            ship.removeMoveDirection(Direction.BACKWARD);
        }

        if (key == KEY_A) {
            core.sendUDPPacket(new PacketShipStopMove(Direction.RIGHT));
            ship.removeMoveDirection(Direction.RIGHT);
        }

        if (key == KEY_D) {
            core.sendUDPPacket(new PacketShipStopMove(Direction.LEFT));
            ship.removeMoveDirection(Direction.LEFT);
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
    public boolean mouseLeftClick() {
        if (guiManager.isActive()) return false;

        if (ship == null) {
            Vector2f mousePosition = Engine.mouse.getWorldPosition(camera);
            List<Ship> ships = core.getWorld().getEntitiesByType(Ship.class);
            for (int i = 0, size = ships.size(); i < size; i++) {
                Ship ship = ships.get(i);
                if (isMouseIntersectsWith(ship, mousePosition.x, mousePosition.y)) {
                    eventBus.publish(new SelectShipEvent(ship));
                    return true;
                }
            }

            eventBus.publish(new SelectShipEvent(null));
        } else {
            core.sendUDPPacket(new PacketMouseLeftClick());
            mouseLeftDown = true;
        }

        return false;
    }

    @Override
    public boolean mouseLeftRelease() {
        if (ship == null) return false;

        core.sendUDPPacket(new PacketMouseLeftRelease());
        mouseLeftDown = false;
        return false;
    }

    @Override
    public boolean mouseRightClick() {
        Vector2f mousePosition = Engine.mouse.getWorldPosition(camera);
        List<Ship> ships = core.getWorld().getEntitiesByType(Ship.class);
        for (int i = 0, size = ships.size(); i < size; i++) {
            Ship ship = ships.get(i);
            if (isMouseIntersectsWith(ship, mousePosition.x, mousePosition.y)) {
                eventBus.publish(new SelectSecondaryShipEvent(ship));
                return true;
            }
        }

        eventBus.publish(new SelectSecondaryShipEvent(null));
        return false;
    }

    private boolean isMouseIntersectsWith(RigidBody rigidBody, float mouseX, float mouseY) {
        AABB aabb = new AABB(0);
        MathUtils.computeAABB(aabb, rigidBody.getBody(), rigidBody.getBody().getTransform(), new AABB(0));
        return aabb.contains(mouseX, mouseY);
    }

    public void setShip(Ship ship) {
        if (this.ship != null) {
            this.ship.resetPositionCalculatorAndChronologicalProcessor();
            this.ship.setControlledByPlayer(false);
        }

        this.ship = ship;

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