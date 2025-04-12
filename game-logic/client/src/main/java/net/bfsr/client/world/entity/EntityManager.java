package net.bfsr.client.world.entity;

import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.CommonEntityManager;

public class EntityManager extends CommonEntityManager {
    @Override
    public void update(double timestamp) {
        for (int i = 0; i < entities.size(); i++) {
            RigidBody rigidBody = entities.get(i);
            if (rigidBody.isDead()) {
                rigidBody.getWorld().remove(i--, rigidBody);

                // Client predicted bullets.
                // Only happens when client has not received actual bullet id from server.
                if (rigidBody.getId() < 0) {
//                    rigidBody.getWorld().getEntityIdManager().returnBackId(rigidBody.getId());
                }
            } else {
                rigidBody.update();
            }
        }
    }
}
