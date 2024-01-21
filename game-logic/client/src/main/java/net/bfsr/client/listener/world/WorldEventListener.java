package net.bfsr.client.listener.world;

import net.bfsr.client.Core;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.entity.RigidBody;
import net.bfsr.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.event.entity.RigidBodyPostPhysicsUpdateEvent;

public class WorldEventListener {
    private final Core core = Core.get();
    private final RenderManager renderManager = core.getRenderManager();

    @EventHandler
    public EventListener<RigidBodyAddToWorldEvent> addToWorldEvent() {
        return event -> renderManager.createRender(event.getRigidBody());
    }

    @EventHandler
    public EventListener<RigidBodyPostPhysicsUpdateEvent> postPhysicsUpdateEvent() {
        return event -> {
            RigidBody<?> rigidBody = event.getRigidBody();
            double renderTime = core.getRenderTime();
            rigidBody.calcPosition(renderTime);
            rigidBody.processChronologicalData(renderTime);
        };
    }
}