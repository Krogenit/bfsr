package net.bfsr.client.listener.entity.bullet;

import net.bfsr.client.Core;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.entity.BulletRender;
import net.bfsr.event.entity.bullet.BulletAddToWorldEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class BulletEventListener {
    private final RenderManager renderManager = Core.get().getWorldRenderer().getRenderManager();

    @Handler
    public void event(BulletAddToWorldEvent event) {
        renderManager.addRender(new BulletRender(event.bullet()));
    }
}