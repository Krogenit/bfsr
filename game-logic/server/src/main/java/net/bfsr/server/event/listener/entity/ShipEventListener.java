package net.bfsr.server.event.listener.entity;

import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
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
import net.bfsr.server.player.Player;
import net.bfsr.world.World;
import org.jbox2d.common.Vector2;

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
            Vector2 linearVelocity = ship.getLinearVelocity();
            float sizeX = ship.getSizeX();
            float sizeY = ship.getSizeY();
            WreckSpawner.spawnDamageDebris(world, 1, ship.getX() - sizeX / 2.5f + rand.nextInt((int) (sizeX / 1.25f)),
                    ship.getY() - sizeY / 2.5f + rand.nextInt((int) (sizeY / 1.25f)), linearVelocity.x * 0.1f, linearVelocity.y * 0.1f,
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