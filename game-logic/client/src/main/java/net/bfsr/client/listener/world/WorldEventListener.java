package net.bfsr.client.listener.world;

import net.bfsr.client.Core;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.event.entity.RigidBodyAddToWorldEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class WorldEventListener {
    private final RenderManager renderManager = Core.get().getRenderManager();

    @Handler
    public void event(RigidBodyAddToWorldEvent event) {
        renderManager.createRender(event.getRigidBody());
    }
}