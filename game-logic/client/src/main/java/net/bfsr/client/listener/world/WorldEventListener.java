package net.bfsr.client.listener.world;

import net.bfsr.client.Core;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.entity.RigidBody;
import net.bfsr.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.event.entity.RigidBodyPostPhysicsUpdateEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class WorldEventListener {
    private final Core core = Core.get();
    private final RenderManager renderManager = core.getRenderManager();

    @Handler
    public void event(RigidBodyAddToWorldEvent event) {
        renderManager.createRender(event.getRigidBody());
    }

    @Handler
    public void event(RigidBodyPostPhysicsUpdateEvent event) {
        RigidBody<?> rigidBody = event.getRigidBody();
        double renderTime = core.getRenderTime();
        rigidBody.calcPosition(renderTime);
        rigidBody.processChronologicalData(renderTime);
    }
}