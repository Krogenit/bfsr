package net.bfsr.world;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.event.EventBus;
import net.bfsr.event.entity.bullet.BulletAddToWorldEvent;
import net.bfsr.event.entity.ship.ShipAddToWorldEvent;
import net.bfsr.event.entity.ship.ShipSpawnEvent;
import net.bfsr.event.entity.wreck.ShipWreckAddToWorldEvent;
import net.bfsr.event.entity.wreck.WreckAddToWorldEvent;
import net.bfsr.physics.CCDTransformHandler;
import net.bfsr.physics.ContactListener;
import net.bfsr.physics.CustomValueMixer;
import net.bfsr.profiler.Profiler;
import net.bfsr.util.ObjectPool;
import net.bfsr.util.Side;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.world.PhysicsWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class World {
    public static final ObjectPool<Wreck> WREAK_POOL = new ObjectPool<>();

    private org.dyn4j.world.World<Body> physicWorld;
    private final CCDTransformHandler ccdTransformHandler = new CCDTransformHandler();
    @Getter
    protected final Side side;

    private final Profiler profiler;
    protected final Random rand = new Random();

    private int nextId;
    private final TIntObjectMap<RigidBody> entitiesById = new TIntObjectHashMap<>();

    protected final List<Ship> ships = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<ShipWreck> shipWrecks = new ArrayList<>();
    private final List<Wreck> wrecks = new ArrayList<>();

    protected World(Profiler profiler, Side side) {
        this.profiler = profiler;
        this.side = side;
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
        updateWrecks();

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
            Ship ship = ships.get(i);
            if (ship.isDead()) {
                removeShip(ship, i--);
            } else {
                ship.update();
            }
        }
    }

    private void updateBullets() {
        for (int i = 0; i < bullets.size(); i++) {
            RigidBody bullet = bullets.get(i);

            if (bullet.isDead()) {
                removeObjectById(bullet.getId());
                physicWorld.removeBody(bullet.getBody());
                bullets.remove(i--);
            } else {
                bullet.update();
            }
        }
    }

    private void updateWrecks() {
        for (int i = 0; i < wrecks.size(); i++) {
            Wreck wreck = wrecks.get(i);
            if (wreck.isDead()) {
                removePhysicObject(wreck);
                wrecks.remove(i--);
                WREAK_POOL.returnBack(wreck);
            } else {
                wreck.update();
            }
        }

        for (int i = 0; i < shipWrecks.size(); i++) {
            ShipWreck wreck = shipWrecks.get(i);
            if (wreck.isDead()) {
                shipWrecks.remove(i--);
                removePhysicObject(wreck);
            } else {
                wreck.update();

                if (wreck.getFixturesToAdd().size() > 0) {
                    wreck.getBody().removeAllFixtures();
                    List<BodyFixture> fixturesToAdd = wreck.getFixturesToAdd();
                    while (fixturesToAdd.size() > 0) {
                        wreck.getBody().addFixture(fixturesToAdd.remove(0));
                    }

                    wreck.getBody().updateMass();
                }
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

        for (int i = 0, size = shipWrecks.size(); i < size; i++) {
            shipWrecks.get(i).postPhysicsUpdate();
        }

        for (int i = 0, size = wrecks.size(); i < size; i++) {
            wrecks.get(i).postPhysicsUpdate();
        }
    }

    protected void removeShip(Ship ship, int index) {
        physicWorld.removeBody(ship.getBody());
        ship.clear();
        ships.remove(index);
        removeObjectById(ship.getId());
    }

    public void addShip(Ship ship) {
        entitiesById.put(ship.getId(), ship);
        ships.add(ship);
        EventBus.post(side, new ShipAddToWorldEvent(ship));
    }

    public void spawnShip(Ship ship) {
        physicWorld.addBody(ship.getBody());
        EventBus.post(side, new ShipSpawnEvent(ship));
    }

    public void addBullet(Bullet bullet) {
        entitiesById.put(bullet.getId(), bullet);
        bullets.add(bullet);
        physicWorld.addBody(bullet.getBody());
        EventBus.post(side, new BulletAddToWorldEvent(bullet));
    }

    public void addWreck(Wreck wreck) {
        wrecks.add(wreck);
        addPhysicObject(wreck);
        EventBus.post(side, new WreckAddToWorldEvent(wreck));
    }

    public void addWreck(ShipWreck wreck) {
        shipWrecks.add(wreck);
        addPhysicObject(wreck);
        EventBus.post(side, new ShipWreckAddToWorldEvent(wreck));
    }

    public void addPhysicObject(RigidBody rigidBody) {
        entitiesById.put(rigidBody.getId(), rigidBody);
        physicWorld.addBody(rigidBody.getBody());
    }

    public void removePhysicObject(RigidBody rigidBody) {
        removeObjectById(rigidBody.getId());
        physicWorld.removeBody(rigidBody.getBody());
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
        for (int i = 0; i < ships.size(); i++) {
            ships.get(i).clear();
        }

        ships.clear();
        bullets.clear();

        for (int i = 0; i < wrecks.size(); i++) {
            WREAK_POOL.returnBack(wrecks.get(i));
        }

        wrecks.clear();
    }

    public int getBulletsCount() {
        return bullets.size();
    }

    public int getWreckCount() {
        return wrecks.size();
    }

    public int getShipWreckCount() {
        return shipWrecks.size();
    }

    public List<Ship> getShips() {
        return ships;
    }

    public RigidBody getEntityById(int id) {
        return entitiesById.get(id);
    }

    public int getNextId() {
        return nextId++;
    }

    public boolean isServer() {
        return side.isServer();
    }

    public boolean isClient() {
        return side.isClient();
    }
}