package net.bfsr.server.event.listener.entity;

import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.ship.*;
import net.bfsr.network.packet.server.entity.ship.PacketDestroyingShip;
import net.bfsr.network.packet.server.entity.ship.PacketShipInfo;
import net.bfsr.network.packet.server.entity.ship.PacketSyncMoveDirection;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.player.Player;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.util.Random;

public class ShipEventListener {
    private final EntityTrackingManager trackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();

    @EventHandler
    public EventListener<ShipNewMoveDirectionEvent> shipNewMoveDirectionEvent() {
        return event -> {
            Ship ship = event.ship();
            if (ship.isControlledByPlayer()) {
                Player player = ServerGameLogic.getInstance().getPlayerManager().getPlayerControllingShip(ship);
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
                Player player = ServerGameLogic.getInstance().getPlayerManager().getPlayerControllingShip(ship);
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
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(),
                    new PacketShipInfo(ship, ship.getWorld().getTimestamp()));
        };
    }

    @EventHandler
    public EventListener<ShipDestroyingEvent> shipDestroyingEvent() {
        return event -> {
            Ship ship = event.ship();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketDestroyingShip(ship,
                    ship.getWorld().getTimestamp()));
        };
    }

    @EventHandler
    public EventListener<ShipDestroyingExplosionEvent> shipDestroyingExplosionEvent() {
        return event -> {
            Ship ship = event.ship();
            World world = ship.getWorld();
            Random rand = world.getRand();
            Vector2f position = ship.getPosition();
            Vector2f velocity = ship.getVelocity();
            Vector2f size = ship.getSize();
            WreckSpawner.spawnDamageDebris(world, 1, position.x - size.x / 2.5f + rand.nextInt((int) (size.x / 1.25f)),
                    position.y - size.y / 2.5f + rand.nextInt((int) (size.y / 1.25f)), velocity.x * 0.1f, velocity.y * 0.1f,
                    1.0f);
        };
    }

    @EventHandler
    public EventListener<ShipDestroyEvent> shipDestroyEvent() {
        return event -> {
            Ship ship = event.ship();
            WreckSpawner.spawnDestroyShipSmall(ship);
        };
    }
}