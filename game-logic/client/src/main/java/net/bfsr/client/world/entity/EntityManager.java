package net.bfsr.client.world.entity;

import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.engine.RenderDelayChangeEvent;
import net.bfsr.engine.physics.correction.CorrectionHandler;
import net.bfsr.engine.physics.correction.DynamicCorrectionHandler;
import net.bfsr.engine.physics.correction.HistoryCorrectionHandler;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.CommonEntityManager;
import org.jbox2d.dynamics.BodyType;

public class EntityManager extends CommonEntityManager {
    private final EventBus eventBus;

    public EntityManager(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Override
    public void update(int frame) {
        for (int i = 0; i < entities.size(); i++) {
            RigidBody rigidBody = entities.get(i);
            if (rigidBody.isDead()) {
                rigidBody.getWorld().remove(i--, rigidBody, frame);
            } else {
                rigidBody.update();
            }
        }
    }

    @EventHandler
    public EventListener<RenderDelayChangeEvent> onRenderDelayChange() {
        return event -> {
            for (int i = 0; i < entities.size(); i++) {
                RigidBody rigidBody = entities.get(i);
                if (rigidBody.getBody().getType() == BodyType.STATIC) {
                    continue;
                }

                rigidBody.setCorrectionHandler(new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(0.5f), new CorrectionHandler(),
                        new HistoryCorrectionHandler()));
            }
        };
    }

    @Override
    public void clear() {
        super.clear();
        eventBus.unregister(this);
    }
}
