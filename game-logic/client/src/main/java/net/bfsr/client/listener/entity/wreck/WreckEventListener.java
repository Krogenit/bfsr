package net.bfsr.client.listener.entity.wreck;

import net.bfsr.client.Core;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.wreck.WreckDeathEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.joml.Vector2f;

@Listener(references = References.Strong)
public class WreckEventListener {
    private final RenderManager renderManager = Core.get().getRenderManager();

    @Handler
    public void event(WreckDeathEvent event) {
        Wreck wreck = event.wreck();
        Render<?> render = renderManager.getRender(wreck.getId());
        if (render != null && render.getColor().w > 0.01f) {
            Vector2f pos = wreck.getPosition();
            ExplosionEffects.spawnSmallExplosion(pos.x, pos.y, wreck.getSize().x);
        }
    }
}