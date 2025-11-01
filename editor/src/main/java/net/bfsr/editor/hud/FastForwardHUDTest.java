package net.bfsr.editor.hud;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.Client;
import net.bfsr.client.physics.CollisionHandler;
import net.bfsr.client.renderer.entity.BulletRender;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.world.entity.ClientEntityIdManager;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.EntityDataHistoryManager;
import net.bfsr.engine.world.entity.EntityPositionHistory;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.engine.world.entity.TransformData;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.physics.collision.CollisionMatrix;
import net.bfsr.server.entity.EntityManager;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.joml.Vector2f;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
class FastForwardHUDTest {
    private final EditorHUD hud;

    private boolean fastForwardTesting;
    private final Vector2f shipVelocity = new Vector2f(0.01f, 0.01f);
    private final EntityPositionHistory positionHistory = new EntityPositionHistory(Engine.convertSecondsToFrames(5));
    private Ship fastForwardShip;
    private World world;
    private Bullet bullet;
    private final Vector2f maxPosition = new Vector2f(4, 4);
    private Rectangle bulletAABB;
    private boolean bulletAdded;
    private boolean update = true;

    void toggle() {
        fastForwardTesting = !fastForwardTesting;

        if (fastForwardTesting) {
            Client client = Client.get();
            world = new World(new Profiler(), 0L, new EventBus(), new EntityManager(), new ClientEntityIdManager(client),
                    client, new CollisionMatrix(new CollisionHandler(client)));
            world.init();

            ShipRegistry shipRegistry = client.getConfigConverterManager().getConverter(ShipRegistry.class);
            fastForwardShip = new Ship(shipRegistry.get(3));
            fastForwardShip.init(world, 0);
            fastForwardShip.setFaction(Faction.HUMAN);
            fastForwardShip.setSpawned();
            client.getShipOutfitter().outfit(fastForwardShip);
            world.add(fastForwardShip);

            ShipRender shipRender = new ShipRender(fastForwardShip);
            shipRender.init();
            client.getEntityRenderer().addRender(shipRender);

            bullet = createBullet();
        } else {
            fastForwardShip.setDead();
            bullet.setDead();
            world.clear();
        }
    }

    void update() {
        if (fastForwardTesting && !Client.get().isPaused() && update) {
            float x = fastForwardShip.getX();
            float y = fastForwardShip.getY();

            double renderTime = Client.get().getRenderTime();
            int renderFrame = Client.get().getRenderFrame();
            positionHistory.addData(x, y, fastForwardShip.getSin(), fastForwardShip.getCos(), renderFrame);

            fastForwardShip.setPosition(x + shipVelocity.x, y + shipVelocity.y);

            if (fastForwardShip.getX() > maxPosition.x * 0.4f) {
                fastForwardTesting(renderFrame);
            }

            world.update(renderTime, renderFrame);
        }
    }

    private void fastForwardTesting(int renderFrame) {
        if (!bulletAdded) {
            bulletAdded = true;
            bullet.getBody().setActive(true);
            world.add(bullet);

            float additionalSeconds = 0.5f;
            int fastForwardTimeInFrames = Client.get().getRenderDelayManager().getRenderDelayInFrames() +
                    Engine.convertSecondsToFrames(additionalSeconds);

            float updateDeltaTime = Engine.getUpdateDeltaTimeInSeconds();
            float updateDeltaTimeInMills = updateDeltaTime * 1000.0f;
            float updateDeltaTimeInNanos = updateDeltaTimeInMills * 1_000_000.0f;
            int iterations = Math.round(fastForwardTimeInFrames / 1_000_000.0f / updateDeltaTimeInMills);

            float bulletX = bullet.getX();
            float bulletY = bullet.getY();
            Vector2 linearVelocity = bullet.getLinearVelocity();
            float offset = 1.0f + Math.max(bullet.getSizeX(), bullet.getSizeY());
            float endX = bulletX + linearVelocity.x * updateDeltaTime * iterations;
            float endY = bulletY + linearVelocity.y * updateDeltaTime * iterations;
            float minX = Math.min(bulletX, endX) - offset;
            float minY = Math.min(bulletY, endY) - offset;
            float maxX = Math.max(bulletX, endX) + offset;
            float maxY = Math.max(bulletY, endY) + offset;
            AbstractCamera camera = Client.get().getCamera();
            float zoom = camera.getZoom();

            float guiX = (minX - camera.getPosition().x) * zoom - camera.getOrigin().x;
            float guiY = (minY - camera.getPosition().y) * zoom - camera.getOrigin().y;
            float width = (maxX - camera.getPosition().x) * zoom - camera.getOrigin().x - guiX;
            float height = (maxY - camera.getPosition().y) * zoom - camera.getOrigin().y - guiY;

            bulletAABB = new Rectangle((int) width, (int) height);
            bulletAABB.setAllColors(1, 0, 0, 0.5f);
            hud.add(bulletAABB.atBottomLeft((int) guiX, (int) guiY));

            if (iterations > 0) {
                detectAndFastForward(new AABB(new Vector2(minX, minY), new Vector2(maxX, maxY)), iterations,
                        fastForwardTimeInFrames, updateDeltaTimeInNanos, renderFrame);
                return;
            }
        }

        if (fastForwardShip.getX() > maxPosition.x) {
            bulletAdded = false;
            fastForwardShip.setPosition(0.0f, 0.0f);
            bullet = createBullet();
            hud.remove(bulletAABB);
        }
    }

    private void detectAndFastForward(AABB aabb, int iterations, int fastForwardTimeInFrames, float updateDeltaTimeInNanos,
                                      int renderFrame) {
        Set<Body> affectedBodies = new HashSet<>();

        world.getPhysicWorld().queryAABB(fixture -> {
            Body body = fixture.getBody();
            if (affectedBodies.contains(body)) {
                return true;
            }

            if (body == bullet.getBody()) {
                return true;
            }

            affectedBodies.add(body);
            return true;
        }, aabb);

        EntityDataHistoryManager entityDataHistoryManager = new EntityDataHistoryManager();

        for (Body body : affectedBodies) {
            RigidBody rigidBody = (RigidBody) body.getUserData();
            entityDataHistoryManager.addPositionData(rigidBody.getId(), rigidBody.getX(), rigidBody.getY(),
                    rigidBody.getSin(), rigidBody.getCos(), 0);
        }

        update = false;

        world.getPhysicWorld().beginFastForward();

        fastForwardIterationThread(0, iterations, affectedBodies, fastForwardTimeInFrames,
                updateDeltaTimeInNanos, entityDataHistoryManager, renderFrame, fastForwardTimeInFrames);
    }

    private void fastForwardIterationThread(int i, int max, Set<Body> affectedBodies, double fastForwardTimeInNanos,
                                            float updateDeltaTimeInNanos, EntityDataHistoryManager entityDataHistoryManager,
                                            int renderFrame, int fastForwardTimeInFrames) {
        Thread thread = Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            fastForwardIteration(i, max, affectedBodies, fastForwardTimeInNanos, updateDeltaTimeInNanos,
                    entityDataHistoryManager, renderFrame, fastForwardTimeInFrames);
        });
        thread.start();
    }

    private void fastForwardIteration(int i, int max, Set<Body> affectedBodies, double fastForwardTimeInNanos,
                                      float updateDeltaTimeInNanos, EntityDataHistoryManager entityDataHistoryManager,
                                      int renderFrame, int fastForwardTimeInFrames) {
        Client.get().addFutureTask(() -> fastForwardIterationTask(i, max, affectedBodies, fastForwardTimeInNanos, updateDeltaTimeInNanos,
                entityDataHistoryManager, renderFrame, fastForwardTimeInFrames));
    }

    private void fastForwardIterationTask(int i, int max, Set<Body> affectedBodies, double fastForwardTimeInNanos,
                                          float updateDeltaTimeInNanos, EntityDataHistoryManager entityDataHistoryManager,
                                          int renderFrame, int fastForwardTimeInFrames) {
        EntityDataHistoryManager dataHistoryManager = world.getEntityManager().getDataHistoryManager();

        for (Body body : affectedBodies) {
            RigidBody rigidBody = (RigidBody) body.getUserData();
            TransformData transformData = dataHistoryManager.getTransformData(rigidBody.getId(),
                    renderFrame - fastForwardTimeInFrames);
            if (transformData != null) {
                Vector2f position = transformData.getPosition();
                body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
            }
        }

        bullet.update();
        world.getPhysicWorld().fastForwardStep(Engine.getUpdateDeltaTimeInSeconds(), Collections.singletonList(bullet.getBody()));
        bullet.postPhysicsUpdate();
        fastForwardShip.postPhysicsUpdate();

        if (i + 1 == max) {
            hud.remove(bulletAABB);
            world.getPhysicWorld().endFastForward();
            update = true;

            for (Body body : affectedBodies) {
                RigidBody rigidBody = (RigidBody) body.getUserData();
                TransformData transformData = entityDataHistoryManager.getFirstTransformData(rigidBody.getId());
                Vector2f position = transformData.getPosition();
                body.setTransform(position.x, position.y, transformData.getSin(), transformData.getCos());
            }
        } else {
            fastForwardIteration(i + 1, max, affectedBodies, fastForwardTimeInNanos - updateDeltaTimeInNanos,
                    updateDeltaTimeInNanos, entityDataHistoryManager, renderFrame, fastForwardTimeInFrames - 1);
        }
    }

    private Bullet createBullet() {
        Client client = Client.get();
        GunRegistry gunRegistry = client.getConfigConverterManager().getConverter(GunRegistry.class);
        GunData gunData = gunRegistry.get(0);
        float angle = MathUtils.HALF_PI * 0.5f;
        bullet = new Bullet(-0.5f, -0.5f, LUT.sin(angle), LUT.cos(angle), gunData, null,
                gunData.getDamage().copy());
        bullet.init(world, 1);
        bullet.getBody().setActive(false);

        BulletRender bulletRender = new BulletRender(bullet);
        bulletRender.init();
        client.getEntityRenderer().addRender(bulletRender);

        return bullet;
    }

    void remove() {
        if (fastForwardShip != null) {
            fastForwardShip.setDead();
            bullet.setDead();
            world.clear();
        }
    }
}
