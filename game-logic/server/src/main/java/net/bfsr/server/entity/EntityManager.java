package net.bfsr.server.entity;

import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.CommonEntityManager;

public class EntityManager extends CommonEntityManager {
    @Override
    protected void update(RigidBody rigidBody, int frame) {
        super.update(rigidBody, frame);
        if (rigidBody.getLifeTime() >= rigidBody.getMaxLifeTime()) {
            rigidBody.setDead();
        } else {
            getDataHistoryManager().addPositionData(rigidBody.getId(), rigidBody.getX(), rigidBody.getY(), rigidBody.getSin(),
                    rigidBody.getCos(), frame);
        }
    }
}