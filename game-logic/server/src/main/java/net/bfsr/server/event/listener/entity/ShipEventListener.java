package net.bfsr.server.event.listener.entity;

import lombok.RequiredArgsConstructor;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.ship.ShipDestroyEvent;
import net.bfsr.event.entity.ship.ShipDestroyingEvent;
import net.bfsr.event.entity.ship.ShipNewMoveDirectionEvent;
import net.bfsr.event.entity.ship.ShipRemoveMoveDirectionEvent;
import net.bfsr.event.entity.ship.ShipUpdateEvent;
import net.bfsr.network.packet.server.entity.ship.PacketShipInfo;
import net.bfsr.network.packet.server.entity.ship.PacketShipSetDestroying;
import net.bfsr.network.packet.server.entity.ship.PacketShipSyncMoveDirection;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.physics.CollisionHandler;

@RequiredArgsConstructor
public class ShipEventListener {
    private final ServerGameLogic gameLogic = ServerGameLogic.get();
    private final WreckSpawner wreckSpawner = gameLogic.getWreckSpawner();
    private final EntityTrackingManager trackingManager = gameLogic.getEntityTrackingManager();
    private final CollisionHandler collisionHandler = gameLogic.getCollisionHandler();
    private final DamageSystem damageSystem = gameLogic.getDamageSystem();

    @EventHandler
    public EventListener<ShipNewMoveDirectionEvent> shipNewMoveDirectionEvent() {
        return event -> {
            Ship ship = event.ship();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShipSyncMoveDirection(ship.getId(),
                    event.direction().ordinal(), false, gameLogic.getFrame()));
        };
    }

    @EventHandler
    public EventListener<ShipRemoveMoveDirectionEvent> shipRemoveMoveDirectionEvent() {
        return event -> {
            Ship ship = event.ship();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShipSyncMoveDirection(ship.getId(),
                    event.direction().ordinal(), true, gameLogic.getFrame()));
        };
    }

    @EventHandler
    public EventListener<ShipUpdateEvent> shipUpdateEvent() {
        return event -> {
            Ship ship = event.ship();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShipInfo(ship, gameLogic.getFrame()));
        };
    }

    @EventHandler
    public EventListener<ShipDestroyingEvent> shipDestroyingEvent() {
        return event -> {
            Ship ship = event.ship();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(),
                    new PacketShipSetDestroying(ship, gameLogic.getFrame()));
        };
    }

    @EventHandler
    public EventListener<ShipDestroyEvent> shipDestroyEvent() {
        return event -> {
            Ship ship = event.ship();
            damageSystem.createDestroyedShipWrecks(ship);
            wreckSpawner.onSmallShipDestroy(ship);

            float maxShipSize = Math.max(ship.getSizeX(), ship.getSizeY());
            float waveRadius = maxShipSize * 1.1f;
            float wavePower = maxShipSize * 0.01f;
            collisionHandler.createWave(ship.getWorld(), ship.getX(), ship.getY(), waveRadius, wavePower);
        };
    }
}