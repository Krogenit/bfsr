package net.bfsr.server.event.listener.entity.wreck;

import clipper2.core.Path64;
import net.bfsr.damage.DamageUtils;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.wreck.*;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.packet.common.PacketObjectPosition;
import net.bfsr.server.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.server.network.packet.server.entity.wreck.PacketShipWreck;
import net.bfsr.server.world.WorldServer;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.dyn4j.dynamics.Body;

@Listener(references = References.Strong)
public class WreckEventListener {
    private final NetworkSystem network = ServerGameLogic.getNetwork();
    private final WorldServer world = ServerGameLogic.getInstance().getWorld();

    @Handler
    public void event(WreckUpdateEvent event) {
        Wreck wreck = event.wreck();
        network.sendUDPPacketToAllNearby(new PacketObjectPosition(wreck), wreck.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Handler
    public void event(WreckDeathEvent event) {
        Wreck wreck = event.wreck();
        network.sendTCPPacketToAll(new PacketRemoveObject(wreck));
    }

    @Handler
    public void event(ShipWreckAddToWorldEvent event) {
        ShipWreck wreck = event.wreck();
        network.sendTCPPacketToAllNearby(new PacketShipWreck(wreck), wreck.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Handler
    public void event(BulletDamageShipWreckEvent event) {
        ShipWreck wreck = event.wreck();
        Body body = wreck.getBody();
        float polygonRadius = 1.75f;
        float radius = 4.0f;

        double x = body.getTransform().getTranslationX();
        double y = body.getTransform().getTranslationY();
        double sin = body.getTransform().getSint();
        double cos = body.getTransform().getCost();

        Path64 clip = DamageUtils.createCirclePath(event.contactX() - x, event.contactY() - y, -sin, cos, 12, polygonRadius);
        DamageUtils.damage(wreck, event.contactX(), event.contactY(), clip, radius, world::getNextId);
    }

    @Handler
    public void event(ShipWreckDeathEvent event) {
        network.sendTCPPacketToAll(new PacketRemoveObject(event.wreck().getId()));
    }
}