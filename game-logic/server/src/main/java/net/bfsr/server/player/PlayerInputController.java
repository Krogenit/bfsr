package net.bfsr.server.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.ai.Ai;
import net.bfsr.engine.Engine;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.network.packet.server.component.PacketWeaponShoot;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.entity.EntityTrackingManager;
import org.joml.Vector2f;

@RequiredArgsConstructor
public class PlayerInputController {
    private final Player player;
    private final Vector2f mousePosition = new Vector2f();
    private boolean mouseLeftDown;
    @Getter
    private Ship ship;
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final EntityTrackingManager trackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();
    private final PlayerManager playerManager = ServerGameLogic.getInstance().getPlayerManager();

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
                ship.shoot(weaponSlot -> {
                    weaponSlot.createBullet((float) (Engine.getClientRenderDelayInMills() +
                            player.getNetworkHandler().getPing()));
                    trackingManager.sendPacketToPlayersTrackingEntityExcept(ship.getId(), player1 -> new PacketWeaponShoot(
                            ship.getId(), weaponSlot.getId(), player1.getClientTime(ship.getWorld().getTimestamp())), player);
                });
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
            Ai ai = AiFactory.createAi();
            ai.init(this.ship);
            this.ship.setAi(ai);
            this.ship.removeAllMoveDirections();
            this.ship.setControlledByPlayer(false);
            playerManager.setPlayerControlledShip(player, null);
        }

        this.ship = ship;

        if (ship != null) {
            ship.setAi(Ai.NO_AI);
            ship.removeAllMoveDirections();
            ship.setControlledByPlayer(true);
            playerManager.setPlayerControlledShip(player, ship);
        }
    }
}