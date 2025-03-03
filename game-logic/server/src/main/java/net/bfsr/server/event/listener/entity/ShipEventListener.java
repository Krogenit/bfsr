package net.bfsr.server.event.listener.entity;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.RequiredArgsConstructor;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.util.RandomHelper;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.ship.ShipDestroyEvent;
import net.bfsr.event.entity.ship.ShipDestroyingEvent;
import net.bfsr.event.entity.ship.ShipDestroyingExplosionEvent;
import net.bfsr.event.entity.ship.ShipNewMoveDirectionEvent;
import net.bfsr.event.entity.ship.ShipPostPhysicsUpdate;
import net.bfsr.event.entity.ship.ShipRemoveMoveDirectionEvent;
import net.bfsr.network.packet.server.entity.ship.PacketDestroyingShip;
import net.bfsr.network.packet.server.entity.ship.PacketShipInfo;
import net.bfsr.network.packet.server.entity.ship.PacketSyncMoveDirection;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.physics.CollisionHandler;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;
import org.jbox2d.common.Vector2;

@RequiredArgsConstructor
public class ShipEventListener {
    private final ServerGameLogic serverGameLogic = ServerGameLogic.get();
    private final WreckSpawner wreckSpawner = serverGameLogic.getWreckSpawner();
    private final EntityTrackingManager trackingManager = serverGameLogic.getEntityTrackingManager();
    private final PlayerManager playerManager = serverGameLogic.getPlayerManager();
    private final CollisionHandler collisionHandler = serverGameLogic.getCollisionHandler();
    private final DamageSystem damageSystem = serverGameLogic.getDamageSystem();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();

    @EventHandler
    public EventListener<ShipNewMoveDirectionEvent> shipNewMoveDirectionEvent() {
        return event -> {
            Ship ship = event.ship();
            if (ship.isControlledByPlayer()) {
                Player player = playerManager.getPlayerControllingShip(ship);
                trackingManager.sendPacketToPlayersTrackingEntityExcept(ship.getId(), new PacketSyncMoveDirection(ship.getId(),
                        event.direction().ordinal(), false, ship.getWorld().getTimestamp()), player);
            } else {
                trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketSyncMoveDirection(ship.getId(),
                        event.direction().ordinal(), false, ship.getWorld().getTimestamp()));
            }
        };
    }

    @EventHandler
    public EventListener<ShipRemoveMoveDirectionEvent> shipRemoveMoveDirectionEvent() {
        return event -> {
            Ship ship = event.ship();
            if (ship.isControlledByPlayer()) {
                Player player = playerManager.getPlayerControllingShip(ship);
                trackingManager.sendPacketToPlayersTrackingEntityExcept(ship.getId(), new PacketSyncMoveDirection(ship.getId(),
                        event.direction().ordinal(), true, ship.getWorld().getTimestamp()), player);
            } else {
                trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketSyncMoveDirection(ship.getId(),
                        event.direction().ordinal(), true, ship.getWorld().getTimestamp()));
            }
        };
    }

    @EventHandler
    public EventListener<ShipPostPhysicsUpdate> shipPostPhysicsUpdateEvent() {
        return event -> {
            Ship ship = event.ship();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShipInfo(ship, ship.getWorld().getTimestamp()));
        };
    }

    @EventHandler
    public EventListener<ShipDestroyingEvent> shipDestroyingEvent() {
        return event -> {
            Ship ship = event.ship();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketDestroyingShip(ship, ship.getWorld().getTimestamp()));
        };
    }

    @EventHandler
    public EventListener<ShipDestroyingExplosionEvent> shipDestroyingExplosionEvent() {
        return event -> {
            Ship ship = event.ship();
            World world = ship.getWorld();
            Vector2 linearVelocity = ship.getLinearVelocity();
            float halfSizeX = ship.getSizeX() * 0.5f;
            float halfSizeY = ship.getSizeY() * 0.5f;
            wreckSpawner.spawnDamageDebris(world, 1, ship.getX() + RandomHelper.randomFloat(random, -halfSizeX, halfSizeX),
                    ship.getY() + RandomHelper.randomFloat(random, -halfSizeY, halfSizeY), linearVelocity.x * 0.1f, linearVelocity.y * 0.1f,
                    1.0f);
        };
    }

    @EventHandler
    public EventListener<ShipDestroyEvent> shipDestroyEvent() {
        return event -> {
            Ship ship = event.ship();
            damageSystem.createDestroyedShipWrecks(ship);

            wreckSpawner.spawnDestroyShipSmall(ship);

            float maxShipSize = Math.max(ship.getSizeX(), ship.getSizeY());
            float waveRadius = maxShipSize * 1.1f;
            float wavePower = maxShipSize * 0.01f;
            collisionHandler.createWave(ship.getWorld(), ship.getX(), ship.getY(), waveRadius, wavePower);
        };
    }
}