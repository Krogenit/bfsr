package net.bfsr.client.input;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.gui.hud.HUDAdapter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.network.packet.input.*;
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
    private final Vector2f lastMousePosition = new Vector2f();

    @Override
    public void init() {
        core = Core.get();
        guiManager = core.getGuiManager();
        core.subscribe(this);
    }

    @Override
    public void update() {
        if (ship == null || guiManager.isActive()) return;

        if (ship.isDead()) {
            ship = null;
            return;
        }

        if (ship.isSpawned() && ship.getDestroyingTimer() == 0) {
            controlShip();
        }
    }

    @Override
    public boolean input(int key) {
        if (ship == null || guiManager.isActive()) return false;

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

//        Vector2f mouseWorldPosition = Engine.mouse.getWorldPosition(camera);
        Vector2f mouseWorldPosition = new Vector2f(ship.getPosition().x - 100, ship.getPosition().y);
        RigidBodyUtils.rotateToVector(ship, mouseWorldPosition, ship.getModules().getEngine().getAngularVelocity());
        if (mouseWorldPosition.x != lastMousePosition.x || mouseWorldPosition.y != lastMousePosition.y) {
            core.sendUDPPacket(new PacketSyncPlayerMousePosition(mouseWorldPosition));
        }

        ship.getMoveDirections().forEach(ship::move);
    }

    @Override
    public boolean onMouseLeftClick() {
        if (guiManager.isActive()) return false;

        if (ship == null) {
            HUDAdapter guiInGame = guiManager.getHud();
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
        } else {
            core.sendUDPPacket(new PacketMouseLeftClick());
        }

        return false;
    }

    @Override
    public boolean onMouseLeftRelease() {
        if (guiManager.isActive() || ship == null) return false;

        core.sendUDPPacket(new PacketMouseLeftRelease());
        return false;
    }

    @Override
    public boolean onMouseRightClick() {
        if (guiManager.isActive()) return false;

        HUDAdapter guiInGame = guiManager.getHud();
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

    private boolean isMouseIntersectsWith(GameObject gameObject, float mouseX, float mouseY) {
        Vector2f position = gameObject.getPosition();
        Vector2f size = gameObject.getSize();
        float halfWidth = size.x / 2;
        float halfHeight = size.y / 2;
        return mouseX >= position.x - halfWidth && mouseY >= position.y - halfHeight && mouseX < position.x + halfWidth &&
                mouseY < position.y + halfHeight;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
        guiManager.getHud().selectShip(ship);
        guiManager.getHud().onShipControlStarted();
    }

    public boolean isControllingShip() {
        return ship != null;
    }

    @Handler
    public void event(ExitToMainMenuEvent event) {
        ship = null;
    }
}