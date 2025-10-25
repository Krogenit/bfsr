package net.bfsr.client.world.entity;

import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.CommonEntityManager;

public class EntityManager extends CommonEntityManager {
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
}
