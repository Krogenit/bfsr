package net.bfsr.client.listener.world;

import net.bfsr.client.Client;
import net.bfsr.client.renderer.EntityRenderer;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.entity.RigidBodyAddToWorldEvent;
import net.bfsr.engine.event.entity.RigidBodyUpdateEvent;
import net.bfsr.engine.physics.correction.CorrectionHandler;
import net.bfsr.engine.world.entity.RigidBody;
import org.jbox2d.dynamics.BodyType;

public class WorldEventListener {
    private final Client client = Client.get();
    private final EntityRenderer entityRenderer = client.getEntityRenderer();

    @EventHandler
    public EventListener<RigidBodyAddToWorldEvent> addToWorldEvent() {
        return event -> entityRenderer.createRender(event.getRigidBody());
    }

    @EventHandler
    public EventListener<RigidBodyUpdateEvent> rigidBodyUpdateEvent() {
        return event -> {
            RigidBody rigidBody = event.getRigidBody();
            if (rigidBody.getBody().getType() == BodyType.STATIC) {
                return;
            }

            CorrectionHandler correctionHandler = rigidBody.getCorrectionHandler();
            correctionHandler.update(client.getRenderTime(), client.getRenderFrame());
        };
    }
}