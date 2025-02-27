package net.bfsr.client.listener.world;

import net.bfsr.client.Client;
import net.bfsr.client.renderer.EntityRenderer;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.engine.event.entity.RigidBodyPostPhysicsUpdateEvent;

public class WorldEventListener {
    private final Client client = Client.get();
    private final EntityRenderer entityRenderer = client.getEntityRenderer();

    @EventHandler
    public EventListener<RigidBodyAddToWorldEvent> addToWorldEvent() {
        return event -> entityRenderer.createRender(event.getRigidBody());
    }

    @EventHandler
    public EventListener<RigidBodyPostPhysicsUpdateEvent> postPhysicsUpdateEvent() {
        return event -> event.getRigidBody().getCorrectionHandler().update(client.getRenderTime());
    }
}