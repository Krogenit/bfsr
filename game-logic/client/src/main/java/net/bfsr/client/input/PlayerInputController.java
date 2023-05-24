package net.bfsr.client.input;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.event.ExitToMainMenuEvent;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.client.network.packet.client.input.*;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.util.Side;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.EventBus;
import net.bfsr.math.Direction;
import net.bfsr.math.MathUtils;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import org.dyn4j.dynamics.Body;
import org.joml.Vector2f;

import java.util.List;

import static net.bfsr.engine.input.Keys.*;

@Listener
public class PlayerInputController extends InputController {
    @Getter
    private Ship ship;
    private Core core;
    private GuiManager guiManager;
    private final AbstractCamera camera = Engine.renderer.camera;
    private boolean disableLeftClickShipSelection;
    private final Vector2f lastMousePosition = new Vector2f();

    @Override
    public void init() {
        core = Core.get();
        guiManager = core.getGuiManager();
        EventBus.subscribe(Side.CLIENT, this);
    }

    @Override
    public void update() {
        if (ship != null) {
            if (ship.isDead()) {
                ship = null;
            } else if (noActiveGui() && ship.isSpawned()) {
                controlShip();
            }
        }
    }

    @Override
    public boolean input(int key) {
        if (ship != null) {
            if (key == KEY_W) {
                core.sendUDPPacket(new PacketShipMove(Direction.FORWARD));
                ship.addMoveDirection(Direction.FORWARD);
            }

            if (key == KEY_S) {
                core.sendUDPPacket(new PacketShipMove(Direction.BACKWARD));
                ship.addMoveDirection(Direction.BACKWARD);
            }

            if (key == KEY_A) {
                core.sendUDPPacket(new PacketShipMove(Direction.LEFT));
                ship.addMoveDirection(Direction.LEFT);
            }

            if (key == KEY_D) {
                core.sendUDPPacket(new PacketShipMove(Direction.RIGHT));
                ship.addMoveDirection(Direction.RIGHT);
            }

            if (key == KEY_X) {
                core.sendUDPPacket(new PacketShipMove(Direction.STOP));
                ship.addMoveDirection(Direction.STOP);
            }
        }
        return false;
    }

    @Override
    public void release(int key) {
        if (ship != null) {
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
    }

    private void controlShip() {
        if (ship.getDestroyingTimer() == 0) {
            Body body = ship.getBody();
            if (body.isAtRest()) body.setAtRest(false);

            Vector2f mouseWorldPosition = Engine.mouse.getWorldPosition(camera);
            MathUtils.rotateToVector(ship, mouseWorldPosition, ship.getEngine().getAngularVelocity());
            if (mouseWorldPosition.x != lastMousePosition.x || mouseWorldPosition.y != lastMousePosition.y) {
                core.sendUDPPacket(new PacketSyncPlayerMousePosition(mouseWorldPosition));
            }

            ship.getMoveDirections().forEach(ship::move);
        }
    }

    @Override
    public boolean onMouseLeftClick() {
        if (ship == null) {
            if (noActiveGui()) {
                if (disableLeftClickShipSelection) {
                    disableLeftClickShipSelection = false;
                } else {
                    GuiInGame guiInGame = guiManager.getGuiInGame();
                    guiInGame.selectShip(null);
                    Vector2f mousePosition = Engine.mouse.getWorldPosition(camera);
                    List<Ship> ships = core.getWorld().getShips();
                    for (int i = 0, size = ships.size(); i < size; i++) {
                        Ship ship = ships.get(i);
                        if (isMouseIntersectsWith(ship, mousePosition.x, mousePosition.y)) {
                            guiInGame.selectShip(ship);
                            return true;
                        }
                    }
                }
            }
        } else {
            core.sendUDPPacket(new PacketMouseLeftClick());
        }

        return false;
    }

    @Override
    public boolean onMouseLeftRelease() {
        if (ship != null) {
            core.sendUDPPacket(new PacketMouseLeftRelease());
            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseRightClick() {
        GuiInGame guiInGame = guiManager.getGuiInGame();
        guiInGame.selectShipSecondary(null);
        Vector2f mousePosition = Engine.mouse.getWorldPosition(camera);
        List<Ship> ships = core.getWorld().getShips();
        for (int i = 0, size = ships.size(); i < size; i++) {
            Ship ship = ships.get(i);
            if (isMouseIntersectsWith(ship, mousePosition.x, mousePosition.y)) {
                guiInGame.selectShipSecondary(ship);
                return true;
            }
        }

        return false;
    }

    public void disableShipDeselection() {
        disableLeftClickShipSelection = true;
    }

    private boolean isMouseIntersectsWith(GameObject gameObject, float mouseX, float mouseY) {
        Vector2f position = gameObject.getPosition();
        Vector2f size = gameObject.getSize();
        float halfWidth = size.x / 2;
        float halfHeight = size.y / 2;
        return mouseX >= position.x - halfWidth && mouseY >= position.y - halfHeight && mouseX < position.x + halfWidth && mouseY < position.y + halfHeight;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
        guiManager.getGuiInGame().selectShip(ship);
        guiManager.getGuiInGame().onShipControlStarted();
    }

    private boolean noActiveGui() {
        return !guiManager.isActive();
    }

    public boolean isControllingShip() {
        return ship != null;
    }

    @Handler
    public void event(ExitToMainMenuEvent event) {
        ship = null;
    }
}