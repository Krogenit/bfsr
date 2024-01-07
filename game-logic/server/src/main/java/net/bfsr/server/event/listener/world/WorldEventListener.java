package net.bfsr.server.event.listener.world;

import net.bfsr.entity.RigidBody;
import net.bfsr.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.event.entity.RigidBodyDeathEvent;
import net.bfsr.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.network.packet.server.entity.PacketSpawnEntity;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class WorldEventListener {
    private final NetworkSystem network = ServerGameLogic.getNetwork();

    @Handler
    public void event(RigidBodyAddToWorldEvent event) {
        RigidBody<?> rigidBody = event.getRigidBody();
        network.sendTCPPacketToAllNearby(new PacketSpawnEntity(rigidBody.createSpawnData(),
                rigidBody.getWorld().getTimestamp()), rigidBody.getPosition(), TrackingUtils.TRACKING_DISTANCE);
    }

    @Handler
    public void event(RigidBodyDeathEvent event) {
        RigidBody<?> rigidBody = event.getRigidBody();
        network.sendTCPPacketToAllNearby(new PacketRemoveObject(rigidBody.getId(), rigidBody.getWorld().getTimestamp()),
                rigidBody.getPosition(), TrackingUtils.TRACKING_DISTANCE);
    }
}