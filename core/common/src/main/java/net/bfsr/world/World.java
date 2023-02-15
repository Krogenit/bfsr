package net.bfsr.world;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.bfsr.collision.ContactListener;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.BulletCommon;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.physics.CustomValueMixer;
import net.bfsr.profiler.Profiler;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.world.PhysicsWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class World<T extends ShipCommon> {
    protected org.dyn4j.world.World<Body> physicWorld;
    protected final boolean isRemote;
    protected final Profiler profiler;
    protected final Random rand = new Random();
    protected final List<T> ships = new ArrayList<>();
    protected final List<BulletCommon> bullets = new ArrayList<>();
    protected final TIntObjectMap<CollisionObject> entitiesById = new TIntObjectHashMap<>();
    protected int nextId;

    protected World(boolean isRemote, Profiler profiler) {
        this.isRemote = isRemote;
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
        physicWorld.setValueMixer(new CustomValueMixer());
    }

    public void update() {
        updateShips();
        updateBullets();
        updateParticles();

        profiler.endStartSection("physics");
        physicWorld.step(1);
        profiler.endStartSection("postPhysicsUpdate");
        postPhysicsUpdate();
        removeDeadShips();
    }

    private void removeDeadShips() {
        for (int i = 0; i < ships.size(); i++) {
            T s = ships.get(i);
            if (s.isDead()) {
                removeShip(s, i--);
            }
        }
    }

    protected abstract void updateParticles();

    protected void updateShips() {
        for (int i = 0, size = ships.size(); i < size; i++) {
            ships.get(i).update();
        }
    }

    private void updateBullets() {
        for (int i = 0; i < bullets.size(); i++) {
            BulletCommon bullet = bullets.get(i);
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

    protected void removeShip(T ship, int index) {
        physicWorld.removeBody(ship.getBody());
        ship.clear();
        ships.remove(index);
        removeObjectById(ship.getId());
    }

    public void addShip(T ship) {
        entitiesById.put(ship.getId(), ship);
        ships.add(ship);
    }

    public void spawnShip(ShipCommon ship) {
        physicWorld.addBody(ship.getBody());
    }

    public void addBullet(BulletCommon bullet) {
        entitiesById.put(bullet.getId(), bullet);
        bullets.add(bullet);
        physicWorld.addBody(bullet.getBody());
    }

    public void addPhysicObject(CollisionObject collisionObject) {
        entitiesById.put(collisionObject.getId(), collisionObject);
        physicWorld.addBody(collisionObject.getBody());
    }

    public void removePhysicObject(CollisionObject collisionObject) {
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
            ShipCommon s = ships.remove(0);
            s.clear();
        }
        bullets.clear();
    }

    public List<BulletCommon> getBullets() {
        return bullets;
    }

    public List<T> getShips() {
        return ships;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public CollisionObject getEntityById(int id) {
        return entitiesById.get(id);
    }

    public ShipCommon getPlayerShip() {
        return null;
    }

    public int getNextId() {
        while (entitiesById.containsKey(nextId)) {
            nextId++;
        }

        return nextId;
    }
}
