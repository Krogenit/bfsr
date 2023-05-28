package net.bfsr.client.listener.entity.wreck;

import net.bfsr.client.Core;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.entity.ShipWreckRenderer;
import net.bfsr.client.renderer.entity.WreckRender;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.entity.wreck.ShipWreckAddToWorldEvent;
import net.bfsr.event.entity.wreck.ShipWreckFixturesEvent;
import net.bfsr.event.entity.wreck.WreckAddToWorldEvent;
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
        if (render.getColor().w > 0.01f) {
            Vector2f pos = wreck.getPosition();
            ExplosionEffects.spawnSmallExplosion(pos.x, pos.y, wreck.getSize().x);
        }
    }

    @Handler
    public void event(WreckAddToWorldEvent event) {
        renderManager.addRender(new WreckRender(event.wreck()));
    }

    @Handler
    public void event(ShipWreckFixturesEvent event) {
        ShipWreckRenderer render = renderManager.getRender(event.wreck().getId());
        render.computeAABB();
    }

    @Handler
    public void event(ShipWreckAddToWorldEvent event) {
        renderManager.addRender(new ShipWreckRenderer(event.wreck()));
    }
}