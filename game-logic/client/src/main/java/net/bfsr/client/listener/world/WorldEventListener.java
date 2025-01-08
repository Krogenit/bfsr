package net.bfsr.client.listener.world;

import net.bfsr.client.Client;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.event.entity.RigidBodyPostPhysicsUpdateEvent;

public class WorldEventListener {
    private final Client client = Client.get();
    private final RenderManager renderManager = client.getRenderManager();

    @EventHandler
    public EventListener<RigidBodyAddToWorldEvent> addToWorldEvent() {
        return event -> renderManager.createRender(event.getRigidBody());
    }

    @EventHandler
    public EventListener<RigidBodyPostPhysicsUpdateEvent> postPhysicsUpdateEvent() {
        return event -> event.getRigidBody().getCorrectionHandler().update(client.getRenderTime());
    }
}