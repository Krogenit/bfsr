package net.bfsr.server.event.listener.damage;

import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.event.damage.DamageEvent;
import net.bfsr.network.packet.server.entity.PacketSyncDamage;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;

public class DamageEventListener {
    private final EntityTrackingManager entityTrackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();

    @EventHandler
    public EventListener<DamageEvent> damageEvent() {
        return event -> {
            DamageableRigidBody<?> damageable = event.getDamageable();
            entityTrackingManager.sendPacketToPlayersTrackingEntity(damageable.getId(), new PacketSyncDamage(damageable));
        };
    }
}