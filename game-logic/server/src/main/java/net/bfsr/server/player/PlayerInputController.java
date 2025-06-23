package net.bfsr.server.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.ai.Ai;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.RigidBodyUtils;
import net.bfsr.engine.network.LagCompensation;
import net.bfsr.engine.network.packet.server.player.PacketPlayerSyncLocalId;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.network.packet.server.component.PacketWeaponSlotShoot;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.entity.EntityTrackingManager;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PlayerInputController {
    private final Player player;
    private final Vector2f mousePosition = new Vector2f();
    private boolean mouseLeftDown;
    @Getter
    private Ship ship;
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final EntityTrackingManager trackingManager;
    private final PlayerManager playerManager;
    private final AiFactory aiFactory;
    private final LagCompensation lagCompensation = new LagCompensation();

    public void move(Direction direction) {
        if (ship.getModules().getEngines().isEngineAlive(direction)) {
            ship.addMoveDirection(direction);
        }
    }

    public void stopMove(Direction direction) {
        ship.removeMoveDirection(direction);
    }

    public void update(int tick) {
        if (ship != null) {
            rigidBodyUtils.rotateToVector(ship, mousePosition, ship.getModules().getEngines().getAngularVelocity());
            ship.getMoveDirections().forEach(direction -> {
                if (ship.getModules().getEngines().isEngineAlive(direction)) {
                    ship.move(direction);
                }
            });

            if (mouseLeftDown) {
                int fastForwardTimeInTicks = Engine.convertMillisecondsToTicks(Engine.getClientRenderDelayInMills() +
                        player.getNetworkHandler().getAveragePing());
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
                    player.getNetworkHandler().sendUDPPacket(new PacketPlayerSyncLocalId(player.getLocalIdManager().getCurrentId(), tick));
//                    System.out.println("Server send local id " + player.getLocalIdManager().getCurrentId() + " on tick " + tick);
                    lagCompensation.fastForwardBullets(bullets, fastForwardTimeInTicks, ship.getWorld(), tick);
                }

                for (int i = 0; i < weaponSlots.size(); i++) {
                    WeaponSlot weaponSlot = weaponSlots.get(i);
                    trackingManager.sendPacketToPlayersTrackingEntityExcept(ship.getId(), new PacketWeaponSlotShoot(
                            ship.getId(), weaponSlot.getId(), tick), player);
                }
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
            this.ship.setAi(aiFactory.createAi());
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