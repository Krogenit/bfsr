package net.bfsr.world;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.bfsr.client.particle.Particle;
import net.bfsr.client.particle.ParticleRenderer;
import net.bfsr.collision.ContactListener;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.physics.CustomValueMixer;
import net.bfsr.profiler.Profiler;
import org.dyn4j.dynamics.Body;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {

    protected org.dyn4j.world.World<Body> physicWorld;
    protected final boolean isRemote;
    protected final Profiler profiler;
    protected final Random rand = new Random();
    protected final List<Ship> ships = new ArrayList<>();
    protected final List<Bullet> bullets = new ArrayList<>();
    protected final TIntObjectMap<CollisionObject> entitiesById = new TIntObjectHashMap<>();
    protected int nextId = 0;

    public World(boolean isRemote, Profiler profiler) {
        this.isRemote = isRemote;
        this.profiler = profiler;
        initPhysicWorld();
    }

    private void initPhysicWorld() {
        physicWorld = new org.dyn4j.world.World<>();
        physicWorld.setGravity(org.dyn4j.world.World.ZERO_GRAVITY);
        physicWorld.getSettings().setMaximumTranslation(30);
        physicWorld.getSettings().setPositionConstraintSolverIterations(1);
        physicWorld.getSettings().setVelocityConstraintSolverIterations(1);
        physicWorld.addContactListener(new ContactListener());
        physicWorld.setValueMixer(new CustomValueMixer());
//		physicWorld.addListener(new CollisionListener());
    }

    public void update(double delta) {
        updateShips(delta);
        updateBullets(delta);

        profiler.endStartSection("physics");
        physicWorld.step(1);
        profiler.endStartSection("postPhysicsUpdate");
        postPhysicsUpdate(delta);
        removeDeadShips();
    }

    private void removeDeadShips() {
        for (int i = 0; i < ships.size(); i++) {
            Ship s = ships.get(i);
            if (s.isDead()) {
                removeShip(s);
                i--;
            }
        }
    }

    protected void updateShips(double delta) {
        for (Ship s : ships) {
            s.update(delta);
        }
    }

    protected void updateBullets(double delta) {
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta);

            if (bullet.isDead()) {
                removeObjectById(bullet.getId());
                physicWorld.removeBody(bullet.getBody());
                bullets.remove(i);
                i--;
            }
        }
    }

    public void postPhysicsUpdate(double delta) {
        for (Ship s : ships) {
            s.postPhysicsUpdate(delta);
        }

        for (Bullet bullet : bullets) {
            bullet.postPhysicsUpdate(delta);
        }
    }

    protected void removeShip(Ship ship) {
        physicWorld.removeBody(ship.getBody());
        ship.clear();
        ships.remove(ship);
        removeObjectById(ship.getId());
    }

    public void addShip(Ship ship) {
        entitiesById.put(ship.getId(), ship);
        ships.add(ship);
    }

    public void spawnShip(Ship ship) {
        physicWorld.addBody(ship.getBody());
    }

    public void addBullet(Bullet bullet) {
        entitiesById.put(bullet.getId(), bullet);
        bullets.add(bullet);
        physicWorld.addBody(bullet.getBody());
    }

    public void addDynamicParticle(Particle p) {
        entitiesById.put(p.getId(), p);
        physicWorld.addBody(p.getBody());
    }

    public void removeDynamicParticle(Particle p) {
        removeObjectById(p.getId());
        physicWorld.removeBody(p.getBody());
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
            Ship s = ships.remove(0);
            s.clear();
        }
        bullets.clear();
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public CollisionObject getEntityById(int id) {
        return this.entitiesById.get(id);
    }

    public ParticleRenderer getParticleRenderer() {
        return null;
    }

    public Ship getPlayerShip() {
        return null;
    }

    public int getNextId() {
        while (entitiesById.containsKey(nextId)) {
            nextId++;
        }

        return nextId;
    }
}
