package net.bfsr.server.event.listener.entity.ship;

import clipper2.core.Path64;
import net.bfsr.damage.DamageSystem;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.entity.ship.*;
import net.bfsr.network.packet.common.PacketObjectPosition;
import net.bfsr.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.network.packet.server.entity.ship.PacketDestroyingShip;
import net.bfsr.network.packet.server.entity.ship.PacketShipInfo;
import net.bfsr.network.packet.server.entity.ship.PacketSpawnShip;
import net.bfsr.network.packet.server.entity.ship.PacketSyncMoveDirection;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;
import net.bfsr.world.World;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.dyn4j.dynamics.Body;
import org.joml.Vector2f;

import java.util.Random;

@Listener(references = References.Strong)
public class ShipEventListener {
    private final NetworkSystem network = ServerGameLogic.getNetwork();
    private final DamageSystem damageSystem = ServerGameLogic.getInstance().getDamageSystem();

    @Handler
    public void event(ShipAddToWorldEvent event) {
        Ship ship = event.ship();
        network.sendTCPPacketToAllNearby(new PacketSpawnShip(ship), ship.getPosition(), TrackingUtils.PACKET_SPAWN_DISTANCE);
    }

    @Handler
    public void event(ShipNewMoveDirectionEvent event) {
        Ship ship = event.ship();
        network.sendUDPPacketToAllNearby(new PacketSyncMoveDirection(ship.getId(), event.direction().ordinal(), false),
                ship.getPosition(), TrackingUtils.PACKET_UPDATE_DISTANCE);
    }

    @Handler
    public void event(ShipRemoveMoveDirectionEvent event) {
        Ship ship = event.ship();
        network.sendUDPPacketToAllNearby(new PacketSyncMoveDirection(ship.getId(), event.direction().ordinal(), true),
                ship.getPosition(), TrackingUtils.PACKET_UPDATE_DISTANCE);
    }

    @Handler
    public void event(ShipPostPhysicsUpdate event) {
        Ship ship = event.ship();
        Vector2f position = ship.getPosition();
        network.sendUDPPacketToAllNearby(new PacketObjectPosition(ship), position, TrackingUtils.PACKET_UPDATE_DISTANCE);
        network.sendUDPPacketToAllNearby(new PacketShipInfo(ship), position, TrackingUtils.PACKET_UPDATE_DISTANCE);
    }

    @Handler
    public void event(ShipDestroyingEvent event) {
        Ship ship = event.ship();
        network.sendTCPPacketToAllNearby(new PacketDestroyingShip(ship), ship.getPosition(), TrackingUtils.PACKET_SPAWN_DISTANCE);
    }

    @Handler
    public void event(ShipHullDamageEvent event) {
        Ship ship = event.ship();
        Body body = ship.getBody();
        float polygonRadius = 0.75f;
        float radius = 2.0f;

        double x = body.getTransform().getTranslationX();
        double y = body.getTransform().getTranslationY();
        double sin = body.getTransform().getSint();
        double cos = body.getTransform().getCost();

        Path64 clip = damageSystem.createCirclePath(event.contactX() - x, event.contactY() - y, -sin, cos, 12, polygonRadius);

        damageSystem.damage(ship, event.contactX(), event.contactY(), clip, radius);
    }

    @Handler
    public void event(ShipDestroyingExplosionEvent event) {
        Ship ship = event.ship();
        World world = ship.getWorld();

        Random rand = world.getRand();
        Vector2f position = ship.getPosition();
        Vector2f velocity = ship.getVelocity();
        Vector2f size = ship.getSize();
        WreckSpawner.spawnDamageDebris(world, 1, position.x - size.x / 2.5f + rand.nextInt((int) (size.x / 1.25f)),
                position.y - size.y / 2.5f + rand.nextInt((int) (size.y / 1.25f)), velocity.x * 0.1f, velocity.y * 0.1f, 1.0f);
    }

    @Handler
    public void event(ShipDestroyEvent event) {
        Ship ship = event.ship();
        network.sendTCPPacketToAll(new PacketRemoveObject(ship));
        WreckSpawner.spawnDestroyShipSmall(ship);
    }
}