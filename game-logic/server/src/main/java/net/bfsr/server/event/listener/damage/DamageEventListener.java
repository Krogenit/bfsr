package net.bfsr.server.event.listener.damage;

import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.event.damage.DamageEvent;
import net.bfsr.network.packet.server.entity.PacketSyncDamage;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.util.TrackingUtils;

public class DamageEventListener {
    private final NetworkSystem networkSystem = ServerGameLogic.getNetwork();

    @EventHandler
    public EventListener<DamageEvent> damageEvent() {
        return event -> {
            DamageableRigidBody<?> damageable = event.getDamageable();
            networkSystem.sendTCPPacketToAllNearby(new PacketSyncDamage(damageable), damageable.getX(), damageable.getY(),
                    TrackingUtils.TRACKING_DISTANCE);
        };
    }
}