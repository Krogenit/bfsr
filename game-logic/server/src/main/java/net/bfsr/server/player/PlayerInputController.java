package net.bfsr.server.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.math.RigidBodyUtils;
import net.bfsr.network.packet.server.component.PacketWeaponShoot;
import net.bfsr.network.packet.server.entity.PacketSpawnEntity;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.util.TrackingUtils;
import net.bfsr.world.World;
import org.joml.Vector2f;

@RequiredArgsConstructor
public class PlayerInputController {
    private final Vector2f mousePosition = new Vector2f();
    private boolean mouseLeftDown;
    @Getter
    private Ship ship;
    @Setter
    private PlayerNetworkHandler networkHandler;
    private final Player player;
    private final NetworkSystem network = ServerGameLogic.getNetwork();
    private final World world = ServerGameLogic.getInstance().getWorld();
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
                ship.shoot(weaponSlot -> {
                    Ship ship = weaponSlot.getShip();
                    weaponSlot.createBullet((float) (100 + networkHandler.getPing()),
                            bullet -> network.sendUDPPacketToAllNearbyExcept(new PacketSpawnEntity(bullet.createSpawnData(),
                                            world.getTimestamp()), bullet.getPosition(),
                                    TrackingUtils.TRACKING_DISTANCE, player));
                    network.sendUDPPacketToAllNearbyExcept(new PacketWeaponShoot(ship.getId(), weaponSlot.getId(),
                            world.getTimestamp()), ship.getPosition(), TrackingUtils.TRACKING_DISTANCE, player);
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