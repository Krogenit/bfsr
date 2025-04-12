package net.bfsr.engine.network;

import net.bfsr.engine.Engine;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.EntityDataHistoryManager;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.engine.world.entity.TransformData;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LagCompensation {
    public static void fastForwardBullets(List<RigidBody> bullets, float fastForwardTimeInMillis, World world) {
        float updateDeltaTime = Engine.getUpdateDeltaTime();
        float updateDeltaTimeInMills = updateDeltaTime * 1000.0f;
        int iterations = Math.round(fastForwardTimeInMillis / updateDeltaTimeInMills);
        if (iterations > 0) {
            List<Body> bulletsBodies = new ArrayList<>(bullets.size());
            for (int i = 0; i < bullets.size(); i++) {
                bulletsBodies.add(bullets.get(i).getBody());
            }

            RigidBody rigidBody = bullets.get(0);
            Vector2 linearVelocity = rigidBody.getLinearVelocity();
            float x = rigidBody.getX();
            float y = rigidBody.getY();
            float endX = x + linearVelocity.x * updateDeltaTime * iterations;
            float endY = y + linearVelocity.y * updateDeltaTime * iterations;
            float offset = 1.0f + Math.max(rigidBody.getSizeX(), rigidBody.getSizeY());
            float minX = Math.min(x, endX) - offset;
            float minY = Math.min(y, endY) - offset;
            float maxX = Math.max(x, endX) + offset;
            float maxY = Math.max(y, endY) + offset;

            for (int i = 1; i < bullets.size(); i++) {
                rigidBody = bullets.get(i);
                x = rigidBody.getX();
                y = rigidBody.getY();

                offset = 1.0f + Math.max(rigidBody.getSizeX(), rigidBody.getSizeY());
                endX = x + linearVelocity.x * iterations;
                endY = y + linearVelocity.y * iterations;
                minX = Math.min(minX, Math.min(x, endX) - offset);
                minY = Math.min(minY, Math.min(y, endY) - offset);
                maxX = Math.max(maxX, Math.max(x, endX) + offset);
                maxY = Math.max(maxY, Math.max(y, endY) + offset);
            }

            AABB aabb = new AABB(new Vector2(minX, minY), new Vector2(maxX, maxY));

            Set<Body> affectedBodies = new HashSet<>();

            org.jbox2d.dynamics.World physicWorld = world.getPhysicWorld();
            physicWorld.queryAABB(fixture -> {
                Body body = fixture.getBody();
                if (affectedBodies.contains(body)) {
                    return true;
                }

                if (bulletsBodies.contains(body)) {
                    return true;
                }

                affectedBodies.add(body);
                return true;
            }, aabb);

            EntityDataHistoryManager entityDataHistoryManager = new EntityDataHistoryManager();

            for (Body body : affectedBodies) {
                rigidBody = (RigidBody) body.getUserData();
                entityDataHistoryManager.addPositionData(rigidBody.getId(), rigidBody.getX(), rigidBody.getY(),
                        rigidBody.getSin(), rigidBody.getCos(), 0);
            }

            physicWorld.beginFastForward();

            double timestamp = world.getTimestamp();
            float fastForwardTimeInNanos = fastForwardTimeInMillis * 1_000_000.0f;
            float updateDeltaTimeInNanos = updateDeltaTimeInMills * 1_000_000.0f;
            for (int i = 0; i < iterations; i++) {
                EntityDataHistoryManager dataHistoryManager = world.getEntityManager().getDataHistoryManager();

                for (Body body : affectedBodies) {
                    rigidBody = (RigidBody) body.getUserData();
                    TransformData transformData = dataHistoryManager.getTransformData(rigidBody.getId(),
                            timestamp - fastForwardTimeInNanos);
                    if (transformData != null) {
                        Vector2f position = transformData.getPosition();
                        body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
                    }
                }

                for (int i1 = 0; i1 < bullets.size(); i1++) {
                    bullets.get(i1).update();
                }

                physicWorld.fastForwardStep(updateDeltaTime, bulletsBodies);

                for (int i1 = 0; i1 < bullets.size(); i1++) {
                    bullets.get(i1).postPhysicsUpdate();
                }

                fastForwardTimeInNanos -= updateDeltaTimeInNanos;
            }

            physicWorld.endFastForward();
            for (Body body : affectedBodies) {
                rigidBody = (RigidBody) body.getUserData();
                TransformData transformData = entityDataHistoryManager.getFirstTransformData(rigidBody.getId());
                Vector2f position = transformData.getPosition();
                body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
            }
        }
    }
}
