package net.bfsr.client.listener.module.shield;

import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.event.module.shield.ShieldDamageByCollision;
import org.joml.Vector4f;

public class ShieldEventListener {
    @EventHandler
    public EventListener<ShieldDamageByCollision> shieldDamageByCollisionEvent() {
        return event -> {
            Vector4f color = event.ship().getConfigData().getEffectsColor();
            WeaponEffects.spawnDirectedSpark(event.contactX(), event.contactY(), event.normalX(), event.normalY(), 4.5f,
                    color.x, color.y, color.z, color.w);
        };
    }
}