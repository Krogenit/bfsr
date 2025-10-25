package net.bfsr.server.player;

import lombok.Getter;
import net.bfsr.engine.ai.Ai;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.RigidBodyUtils;
import net.bfsr.engine.network.LagCompensation;
import net.bfsr.engine.network.packet.server.player.PacketPlayerSyncLocalId;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.network.packet.server.component.PacketWeaponSlotShoot;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.physics.LagCompensationRayCastManager;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class PlayerInputController {
    private final Player player;
    private final PlayerNetworkHandler networkHandler;
    private final Vector2f mousePosition = new Vector2f();
    private final boolean[] mouseStates = {false, false};
    private final boolean[] buttonsStates = {false, false, false, false, false};
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final EntityTrackingManager trackingManager;
    private final PlayerManager playerManager;
    private final AiFactory aiFactory;
    private final LagCompensation lagCompensation = new LagCompensation();
    private final LagCompensationRayCastManager lagCompensationRayCastManager;

    @Getter
    private Ship ship;

    PlayerInputController(Player player, PlayerNetworkHandler networkHandler, EntityTrackingManager trackingManager,
                          PlayerManager playerManager, AiFactory aiFactory) {
        this.player = player;
        this.networkHandler = networkHandler;
        this.trackingManager = trackingManager;
        this.playerManager = playerManager;
        this.aiFactory = aiFactory;
        this.lagCompensationRayCastManager = new LagCompensationRayCastManager(networkHandler.getWorld(), lagCompensation);
    }

    public void update(int frame) {
        if (ship != null) {
            rigidBodyUtils.rotateToVector(ship, mousePosition, ship.getModules().getEngines().getAngularVelocity());

            Engines engines = ship.getModules().getEngines();
            // W
            if (buttonsStates[0] && engines.isEngineAlive(Direction.FORWARD)) {
                ship.addMoveDirection(Direction.FORWARD);
            } else {
                ship.removeMoveDirection(Direction.FORWARD);
            }

            // S
            if (buttonsStates[2] && engines.isEngineAlive(Direction.BACKWARD)) {
                ship.addMoveDirection(Direction.BACKWARD);
            } else {
                ship.removeMoveDirection(Direction.BACKWARD);
            }

            // A
            if (buttonsStates[1] && engines.isEngineAlive(Direction.RIGHT)) {
                ship.addMoveDirection(Direction.RIGHT);
            } else {
                ship.removeMoveDirection(Direction.RIGHT);
            }

            // D
            if (buttonsStates[3] && engines.isEngineAlive(Direction.LEFT)) {
                ship.addMoveDirection(Direction.LEFT);
            } else {
                ship.removeMoveDirection(Direction.LEFT);
            }

            // X
            if (buttonsStates[4] && engines.isSomeEngineAlive()) {
                ship.addMoveDirection(Direction.STOP);
            } else {
                ship.removeMoveDirection(Direction.STOP);
            }

            ship.getMoveDirections().forEach(direction -> {
                ship.move(direction);
            });

            if (mouseStates[0]) {
                int fastForwardTimeInFrames = getClientDelayInFrames();
                List<RigidBody> bullets = new ArrayList<>(16);
                List<WeaponSlot> weaponSlots = new ArrayList<>(8);
                ship.shoot(weaponSlot -> {
                    Bullet bullet = weaponSlot.createBullet(false);
                    if (bullet != null) {
                        bullet.setClientId(player.getLocalIdManager().getNextId());
                        bullets.add(bullet);
                        weaponSlots.add(weaponSlot);
                    }
                });

                if (bullets.size() > 0) {
                    networkHandler.sendUDPPacket(new PacketPlayerSyncLocalId(player.getLocalIdManager().getId(), frame));
                    lagCompensation.compensateBullets(bullets, fastForwardTimeInFrames, ship.getWorld(), frame);
                }

                for (int i = 0; i < weaponSlots.size(); i++) {
                    WeaponSlot weaponSlot = weaponSlots.get(i);
                    trackingManager.sendPacketToPlayersTrackingEntityExcept(ship.getId(), new PacketWeaponSlotShoot(
                            ship.getId(), weaponSlot.getId(), frame), player);
                }
            }
        }
    }

    public void setMousePosition(float mouseWorldX, float mouseWorldY) {
        mousePosition.set(mouseWorldX, mouseWorldY);
    }

    public void setShip(Ship ship) {
        if (this.ship != null) {
            this.ship.setAi(aiFactory.createAi());
            this.ship.removeAllMoveDirections();
            this.ship.setControlledByPlayer(false);
            this.ship.setRayCastManager(networkHandler.getWorld().getRayCastManager());
            playerManager.setPlayerControlledShip(player, null);
        }

        this.ship = ship;

        if (ship != null) {
            ship.setAi(Ai.NO_AI);
            ship.removeAllMoveDirections();
            ship.setControlledByPlayer(true);
            lagCompensationRayCastManager.setCompensateTimeInFrames(getClientDelayInFrames());
            ship.setRayCastManager(lagCompensationRayCastManager);
            playerManager.setPlayerControlledShip(player, ship);
        }
    }

    public void setInputStates(boolean[] mouseStates, boolean[] buttonsStates) {
        for (int i = 0; i < this.mouseStates.length; i++) {
            this.mouseStates[i] = mouseStates[i];
        }

        for (int i = 0; i < this.buttonsStates.length; i++) {
            this.buttonsStates[i] = buttonsStates[i];
        }
    }

    private int getClientDelayInFrames() {
        return networkHandler.getRenderDelayInFrames() + networkHandler.getAveragePingInFrames();
    }
}