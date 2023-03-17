package net.bfsr.world;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.bfsr.collision.CCDTransformHandler;
import net.bfsr.collision.ContactListener;
import net.bfsr.entity.GameObject;
import net.bfsr.physics.CustomValueMixer;
import net.bfsr.profiler.Profiler;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.world.PhysicsWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class World<S extends GameObject, B extends GameObject> {
    protected org.dyn4j.world.World<Body> physicWorld;
    protected final Profiler profiler;
    protected final Random rand = new Random();
    protected final List<S> ships = new ArrayList<>();
    protected final List<B> bullets = new ArrayList<>();
    protected final TIntObjectMap<GameObject> entitiesById = new TIntObjectHashMap<>();
    protected int nextId;
    private final CCDTransformHandler ccdTransformHandler = new CCDTransformHandler();

    protected World(Profiler profiler) {
        this.profiler = profiler;
        initPhysicWorld();
    }

    private void initPhysicWorld() {
        physicWorld = new org.dyn4j.world.World<>();
        physicWorld.setGravity(PhysicsWorld.ZERO_GRAVITY);
        physicWorld.getSettings().setMaximumTranslation(30);
        physicWorld.getSettings().setPositionConstraintSolverIterations(1);
        physicWorld.getSettings().setVelocityConstraintSolverIterations(1);
        physicWorld.getSettings().setStepFrequency(TimeUtils.UPDATE_DELTA_TIME);
        physicWorld.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY);
        physicWorld.addContactListener(new ContactListener());
        physicWorld.addTimeOfImpactListener(ccdTransformHandler);
        physicWorld.setValueMixer(new CustomValueMixer());
    }

    public void update() {
        updateShips();
        updateBullets();
        updateParticles();

        profiler.endStartSection("physics");
        ccdTransformHandler.clear();
        physicWorld.step(1);
        ccdTransformHandler.restoreTransforms();
        profiler.endStartSection("postPhysicsUpdate");
        postPhysicsUpdate();
    }

    protected abstract void updateParticles();

    protected void updateShips() {
        for (int i = 0; i < ships.size(); i++) {
            S ship = ships.get(i);
            ship.update();
            if (ship.isDead()) {
                removeShip(ship, i--);
            }
        }
    }

    private void updateBullets() {
        for (int i = 0; i < bullets.size(); i++) {
            GameObject bullet = bullets.get(i);
            bullet.update();

            if (bullet.isDead()) {
                removeObjectById(bullet.getId());
                physicWorld.removeBody(bullet.getBody());
                bullets.remove(i--);
            }
        }
    }

    protected void postPhysicsUpdate() {
        for (int i = 0, size = ships.size(); i < size; i++) {
            ships.get(i).postPhysicsUpdate();
        }

        for (int i = 0, size = bullets.size(); i < size; i++) {
            bullets.get(i).postPhysicsUpdate();
        }
    }

    protected void removeShip(S ship, int index) {
        physicWorld.removeBody(ship.getBody());
        ship.clear();
        ships.remove(index);
        removeObjectById(ship.getId());
    }

    public void addShip(S ship) {
        entitiesById.put(ship.getId(), ship);
        ships.add(ship);
    }

    public void spawnShip(S ship) {
        physicWorld.addBody(ship.getBody());
    }

    public void addBullet(B bullet) {
        entitiesById.put(bullet.getId(), bullet);
        bullets.add(bullet);
        physicWorld.addBody(bullet.getBody());
    }

    public void addPhysicObject(GameObject collisionObject) {
        entitiesById.put(collisionObject.getId(), collisionObject);
        physicWorld.addBody(collisionObject.getBody());
    }

    public void removePhysicObject(GameObject collisionObject) {
        removeObjectById(collisionObject.getId());
        physicWorld.removeBody(collisionObject.getBody());
    }

    protected void removeObjectById(int id) {
        entitiesById.remove(id);
    }

    public org.dyn4j.world.World<Body> getPhysicWorld() {
        return physicWorld;
    }

    public Random getRand() {
        return rand;
    }

    public void clear() {
        while (ships.size() > 0) {
            S s = ships.remove(0);
            s.clear();
        }
        bullets.clear();
    }

    public List<B> getBullets() {
        return bullets;
    }

    public List<S> getShips() {
        return ships;
    }

    public GameObject getEntityById(int id) {
        return entitiesById.get(id);
    }

    public GameObject getPlayerShip() {
        return null;
    }

    public int getNextId() {
        return nextId++;
    }
}