package net.bfsr.server.event.listener.entity;

import clipper2.core.Path64;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.ship.*;
import net.bfsr.network.packet.server.entity.ship.PacketDestroyingShip;
import net.bfsr.network.packet.server.entity.ship.PacketShipInfo;
import net.bfsr.network.packet.server.entity.ship.PacketSyncMoveDirection;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.joml.Vector2f;

import java.util.Random;

public class ShipEventListener {
    private final NetworkSystem network = ServerGameLogic.getNetwork();
    private final DamageSystem damageSystem = ServerGameLogic.getInstance().getDamageSystem();

    @EventHandler
    public EventListener<ShipNewMoveDirectionEvent> shipNewMoveDirectionEvent() {
        return event -> {
            Ship ship = event.ship();
            network.sendUDPPacketToAllNearby(new PacketSyncMoveDirection(ship.getId(), event.direction().ordinal(), false,
                    ship.getWorld().getTimestamp()), ship.getPosition(), TrackingUtils.TRACKING_DISTANCE);
        };
    }

    @EventHandler
    public EventListener<ShipRemoveMoveDirectionEvent> shipRemoveMoveDirectionEvent() {
        return event -> {
            Ship ship = event.ship();
            network.sendUDPPacketToAllNearby(new PacketSyncMoveDirection(ship.getId(), event.direction().ordinal(), true,
                    ship.getWorld().getTimestamp()), ship.getPosition(), TrackingUtils.TRACKING_DISTANCE);
        };
    }

    @EventHandler
    public EventListener<ShipPostPhysicsUpdate> shipPostPhysicsUpdateEvent() {
        return event -> {
            Ship ship = event.ship();
            Vector2f position = ship.getPosition();
            network.sendUDPPacketToAllNearby(new PacketShipInfo(ship, ship.getWorld().getTimestamp()), position,
                    TrackingUtils.TRACKING_DISTANCE);
        };
    }

    @EventHandler
    public EventListener<ShipDestroyingEvent> shipDestroyingEvent() {
        return event -> {
            Ship ship = event.ship();
            network.sendTCPPacketToAllNearby(new PacketDestroyingShip(ship, ship.getWorld().getTimestamp()), ship.getPosition(),
                    TrackingUtils.TRACKING_DISTANCE);
        };
    }

    @EventHandler
    public EventListener<ShipHullDamageEvent> shipHullDamageEvent() {
        return event -> {
            if (event.cell().getValue() > 0) return;

            Ship ship = event.ship();
            Body body = ship.getBody();
            float polygonRadius = 0.5f;
            float radius = 1.00f;

            double x = body.getTransform().getTranslationX();
            double y = body.getTransform().getTranslationY();
            double sin = body.getTransform().getSint();
            double cos = body.getTransform().getCost();

            Path64 clip = damageSystem.createCirclePath(event.contactX() - x, event.contactY() - y, -sin, cos, 12, polygonRadius);

            damageSystem.damage(ship, event.contactX(), event.contactY(), clip, radius);
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