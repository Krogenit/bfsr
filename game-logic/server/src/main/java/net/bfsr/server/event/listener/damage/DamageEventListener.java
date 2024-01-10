package net.bfsr.server.event.listener.damage;

import net.bfsr.damage.Damageable;
import net.bfsr.event.damage.DamageEvent;
import net.bfsr.network.packet.server.entity.PacketSyncDamage;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class DamageEventListener {
    private final NetworkSystem networkSystem = ServerGameLogic.getNetwork();

    @Handler
    public void event(DamageEvent event) {
        Damageable<?> damageable = event.getDamageable();
        networkSystem.sendTCPPacketToAllNearby(new PacketSyncDamage(damageable), damageable.getX(), damageable.getY(),
                TrackingUtils.TRACKING_DISTANCE);
    }
}