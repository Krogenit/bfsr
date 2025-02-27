package net.bfsr.server.entity;

import net.bfsr.engine.entity.RigidBody;
import net.bfsr.entity.CommonEntityManager;

public class EntityManager extends CommonEntityManager {
    @Override
    public void update() {
        for (int i = 0; i < entities.size(); i++) {
            RigidBody rigidBody = entities.get(i);

            if (rigidBody.isDead()) {
                rigidBody.getWorld().remove(i--, rigidBody);
            } else {
                rigidBody.update();

                if (rigidBody.getLifeTime() >= rigidBody.getMaxLifeTime()) {
                    rigidBody.setDead();
                }
            }
        }
    }
}