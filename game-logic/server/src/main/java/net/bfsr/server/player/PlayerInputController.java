package net.bfsr.server.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import org.joml.Vector2f;

@RequiredArgsConstructor
public class PlayerInputController {
    private final Vector2f mousePosition = new Vector2f();
    private boolean mouseLeftDown;
    @Getter
    private Ship ship;
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();

    public void move(Direction direction) {
        if (ship.getModules().getEngines().isEngineAlive(direction)) {
            ship.addMoveDirection(direction);
        }
    }

    public void stopMove(Direction direction) {
        ship.removeMoveDirection(direction);
    }

    public void update() {
        if (ship != null) {
            rigidBodyUtils.rotateToVector(ship, mousePosition, ship.getModules().getEngines().getAngularVelocity());
            ship.getMoveDirections().forEach(ship::move);
            if (mouseLeftDown) {
                ship.shoot(weaponSlot -> ship.getWorld().getEventBus().publish(new WeaponShotEvent(weaponSlot)));
            }
        }
    }

    public void mouseLeftClick() {
        mouseLeftDown = true;
    }

    public void mouseLeftRelease() {
        mouseLeftDown = false;
    }

    public void setMousePosition(Vector2f mousePosition) {
        this.mousePosition.set(mousePosition);
    }

    public void setShip(Ship ship) {
        if (this.ship != null) {
            this.ship.addAI();
            this.ship.removeAllMoveDirections();
            this.ship.setControlledByPlayer(false);
        }

        this.ship = ship;

        if (ship != null) {
            ship.removeAI();
            ship.removeAllMoveDirections();
            ship.setControlledByPlayer(true);
        }
    }
}