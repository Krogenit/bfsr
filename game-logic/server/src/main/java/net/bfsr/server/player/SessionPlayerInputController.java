package net.bfsr.server.player;

import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.RigidBodyUtils;
import net.bfsr.engine.network.LagCompensation;
import net.bfsr.engine.network.packet.server.player.PacketPlayerSyncLocalId;
import net.bfsr.engine.world.entity.EntityIdManager;
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

import java.util.ArrayList;
import java.util.List;

public class SessionPlayerInputController extends PlayerInputController {
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final LagCompensation lagCompensation = new LagCompensation();
    private final EntityIdManager localIdManager = new EntityIdManager(-1) {
        @Override
        public int getNextId() {
            return id--;
        }
    };
    private final LagCompensationRayCastManager lagCompensationRayCastManager;

    public SessionPlayerInputController(Player player,
                                        PlayerNetworkHandler networkHandler,
                                        EntityTrackingManager trackingManager,
                                        AiFactory aiFactory) {
        super(player, networkHandler, trackingManager, aiFactory);
        this.lagCompensationRayCastManager = new LagCompensationRayCastManager(networkHandler.getWorld(), lagCompensation);
    }

    @Override
    public void update(int frame) {
        if (ship != null && ship.isSpawned()) {
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
                        bullet.setClientId(localIdManager.getNextId());
                        bullets.add(bullet);
                        weaponSlots.add(weaponSlot);
                    }
                });

                if (bullets.size() > 0) {
                    networkHandler.sendUDPPacket(new PacketPlayerSyncLocalId(localIdManager.getId(), frame));
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

    @Override
    public void setShip(Ship ship) {
        if (this.ship != null) {
            this.ship.setRayCastManager(networkHandler.getWorld().getRayCastManager());
        }

        super.setShip(ship);

        if (ship != null) {
            lagCompensationRayCastManager.setCompensateTimeInFrames(getClientDelayInFrames());
            ship.setRayCastManager(lagCompensationRayCastManager);
        }
    }

    private int getClientDelayInFrames() {
        return networkHandler.getRenderDelayInFrames() + networkHandler.getAveragePingInFrames();
    }
}
