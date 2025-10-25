package net.bfsr.engine.network;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.bfsr.engine.Engine;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.EntityDataHistoryManager;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.engine.world.entity.TransformData;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public final class LagCompensation {
    private final float updateDeltaTime = Engine.getUpdateDeltaTimeInSeconds();
    private final AABB aabb = new AABB();
    private final ObjectSet<Body> affectedBodies = new ObjectOpenHashSet<>();

    public void compensateBullets(List<RigidBody> bullets, int fastForwardTimeInFrames, World world, int frame) {
        if (fastForwardTimeInFrames <= 0) {
            return;
        }

        int iterations = fastForwardTimeInFrames;
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

        aabb.set(minX, minY, maxX, maxY);

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

        EntityDataHistoryManager dataHistoryManager = world.getEntityManager().getDataHistoryManager();

        for (int i = 0; i < iterations; i++) {
            for (Body body : affectedBodies) {
                rigidBody = (RigidBody) body.getUserData();
                TransformData transformData = dataHistoryManager.getTransformData(rigidBody.getId(),
                        frame - fastForwardTimeInFrames);
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

            fastForwardTimeInFrames -= 1;
        }

        physicWorld.endFastForward();

        for (Body body : affectedBodies) {
            rigidBody = (RigidBody) body.getUserData();
            TransformData transformData = entityDataHistoryManager.getFirstTransformData(rigidBody.getId());
            Vector2f position = transformData.getPosition();
            body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
        }

        affectedBodies.clear();
    }

    public void compensateRay(RigidBody rigidBody, RayCastCallback callback, Vector2 point1, Vector2 point2, int compensateTimeInFrames,
                              World world, int frame) {
        if (compensateTimeInFrames <= 0) {
            world.getPhysicWorld().raycast(callback, point1, point2);
            return;
        }

        float x = point1.x;
        float y = point1.y;
        float endX = point2.x;
        float endY = point2.y;
        float offset = 1.0f;
        float minX = Math.min(x, endX) - offset;
        float minY = Math.min(y, endY) - offset;
        float maxX = Math.max(x, endX) + offset;
        float maxY = Math.max(y, endY) + offset;

        aabb.set(minX, minY, maxX, maxY);

        org.jbox2d.dynamics.World physicWorld = world.getPhysicWorld();
        physicWorld.queryAABB(fixture -> {
            Body body = fixture.getBody();
            if (body == rigidBody.getBody()) {
                return true;
            }

            affectedBodies.add(body);
            return true;
        }, aabb);

        EntityDataHistoryManager entityDataHistoryManager = new EntityDataHistoryManager();

        for (Body body : affectedBodies) {
            RigidBody affectedRigidBody = (RigidBody) body.getUserData();
            entityDataHistoryManager.addPositionData(affectedRigidBody.getId(), affectedRigidBody.getX(), affectedRigidBody.getY(),
                    affectedRigidBody.getSin(), affectedRigidBody.getCos(), 0);
        }

        physicWorld.beginFastForward();

        EntityDataHistoryManager dataHistoryManager = world.getEntityManager().getDataHistoryManager();

        for (Body body : affectedBodies) {
            RigidBody affectedRigidBody = (RigidBody) body.getUserData();
            TransformData transformData = dataHistoryManager.getTransformData(affectedRigidBody.getId(),
                    frame - compensateTimeInFrames);
            if (transformData != null) {
                Vector2f position = transformData.getPosition();
                body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
            }
        }

        world.getPhysicWorld().raycast(callback, point1, point2);

        for (Body body : affectedBodies) {
            RigidBody affectedRigidBody = (RigidBody) body.getUserData();
            TransformData transformData = entityDataHistoryManager.getAndRemoveFirstTransformData(affectedRigidBody.getId());
            Vector2f position = transformData.getPosition();
            body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
        }

        affectedBodies.clear();
    }
}
