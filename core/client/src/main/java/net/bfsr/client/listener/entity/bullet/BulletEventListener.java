package net.bfsr.client.listener.entity.bullet;

import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.Renderer;
import net.bfsr.client.renderer.render.entity.BulletRender;
import net.bfsr.event.entity.bullet.BulletAddToWorldEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class BulletEventListener {
    private final Renderer renderer = Core.get().getRenderer();

    @Handler
    public void event(BulletAddToWorldEvent event) {
        renderer.addRender(new BulletRender(event.bullet()));
    }
}