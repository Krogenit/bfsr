package net.bfsr.server.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.math.MathUtils;
import org.joml.Vector2f;

@RequiredArgsConstructor
public class PlayerInputController {
    private final Player player;
    private final Vector2f mousePosition = new Vector2f();
    private boolean mouseLeftDown;
    @Getter
    private Ship ship;

    public void move(Direction direction) {
        ship.addMoveDirection(direction);
    }

    public void stopMove(Direction direction) {
        ship.removeMoveDirection(direction);
    }

    public void update() {
        if (ship != null) {
            MathUtils.rotateToVector(ship, mousePosition, ship.getEngine().getAngularVelocity());
            ship.getMoveDirections().forEach(ship::move);
            if (mouseLeftDown) {
                ship.shoot();
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
        if (this.ship != null) this.ship.addAI();
        this.ship = ship;
        if (ship != null) ship.removeAI();
    }
}