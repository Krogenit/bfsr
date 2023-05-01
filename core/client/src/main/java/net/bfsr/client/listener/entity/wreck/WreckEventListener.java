package net.bfsr.client.listener.entity.wreck;

import net.bfsr.client.core.Core;
import net.bfsr.client.particle.effect.ExplosionEffects;
import net.bfsr.client.renderer.Renderer;
import net.bfsr.client.renderer.render.Render;
import net.bfsr.client.renderer.render.entity.ShipWreckRenderer;
import net.bfsr.client.renderer.render.entity.WreckRender;
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
    private final Renderer renderer = Core.get().getRenderer();

    @Handler
    public void event(WreckDeathEvent event) {
        Wreck wreck = event.wreck();
        Render<?> render = renderer.getRender(wreck.getId());
        if (render.getColor().w > 0.01f) {
            Vector2f pos = wreck.getPosition();
            ExplosionEffects.spawnSmallExplosion(pos.x, pos.y, wreck.getSize().x);
        }
    }

    @Handler
    public void event(WreckAddToWorldEvent event) {
        renderer.addRender(new WreckRender(event.wreck()));
    }

    @Handler
    public void event(ShipWreckFixturesEvent event) {
        ShipWreckRenderer render = (ShipWreckRenderer) renderer.getRender(event.wreck().getId());
        render.computeAABB();
    }

    @Handler
    public void event(ShipWreckAddToWorldEvent event) {
        renderer.addRender(new ShipWreckRenderer(event.wreck()));
    }
}