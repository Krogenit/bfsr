package net.bfsr.server.event.listener.world;

import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.RigidBody;
import net.bfsr.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.event.entity.RigidBodyDeathEvent;
import net.bfsr.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.network.packet.server.entity.PacketSpawnEntity;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;

public class WorldEventListener {
    private final NetworkSystem network = ServerGameLogic.getNetwork();

    @EventHandler
    public EventListener<RigidBodyAddToWorldEvent> rigidBodyAddToWorldEvent() {
        return event -> {
            RigidBody<?> rigidBody = event.getRigidBody();
            network.sendTCPPacketToAllNearby(new PacketSpawnEntity(rigidBody.createSpawnData(),
                    rigidBody.getWorld().getTimestamp()), rigidBody.getPosition(), TrackingUtils.TRACKING_DISTANCE);
        };
    }

    @EventHandler
    public EventListener<RigidBodyDeathEvent> rigidBodyDeathEvent() {
        return event -> {
            RigidBody<?> rigidBody = event.rigidBody();
            network.sendTCPPacketToAllNearby(new PacketRemoveObject(rigidBody.getId(), rigidBody.getWorld().getTimestamp()),
                    rigidBody.getPosition(), TrackingUtils.TRACKING_DISTANCE);
        };
    }
}