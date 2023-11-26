package net.bfsr.client.listener.module.shield;

import net.bfsr.client.particle.effect.ShieldEffects;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.module.shield.ShieldDamageByCollision;
import net.bfsr.event.module.shield.ShieldRebuildEvent;
import net.bfsr.event.module.shield.ShieldRemoveEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.joml.Vector2f;
import org.joml.Vector4f;

@Listener(references = References.Strong)
public class ShieldEventListener {
    @Handler
    public void event(ShieldRebuildEvent event) {
        Ship ship = event.shield().getShip();
        Vector2f position = ship.getPosition();
        Vector4f shipEffectColor = ship.getConfigData().getEffectsColor();
        ShieldEffects.rebuild(position.x, position.y, ship.getSize().x * 2.0f, shipEffectColor.x, shipEffectColor.y,
                shipEffectColor.z, 1.0f);
    }

    @Handler
    public void event(ShieldRemoveEvent event) {
        Ship ship = event.shield().getShip();
        Vector2f position = ship.getPosition();
        Vector4f shipEffectColor = ship.getConfigData().getEffectsColor();
        ShieldEffects.disable(position.x, position.y, ship.getSize().x * 2.0f, shipEffectColor.x, shipEffectColor.y,
                shipEffectColor.z, 1.0f);
    }

    @Handler
    public void event(ShieldDamageByCollision event) {
        Vector4f color = event.ship().getConfigData().getEffectsColor();
        WeaponEffects.spawnDirectedSpark(event.contactX(), event.contactY(), event.normalX(), event.normalY(), 4.5f,
                color.x, color.y, color.z, color.w);
    }
}