package net.bfsr.server.event.listener.damage;

import net.bfsr.damage.Damageable;
import net.bfsr.event.damage.DamageEvent;
import net.bfsr.server.core.Server;
import net.bfsr.server.network.packet.server.entity.wreck.PacketSyncDamage;
import net.bfsr.server.world.WorldServer;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class DamageEventListener {
    @Handler
    public void event(DamageEvent event) {
        Damageable damageable = event.damageable();
        Server.getNetwork().sendTCPPacketToAllNearby(new PacketSyncDamage(damageable), damageable.getX(), damageable.getY(), WorldServer.PACKET_UPDATE_DISTANCE);
    }
}