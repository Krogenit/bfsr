package net.bfsr.engine.logic;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.loop.AbstractGameLoop;
import net.bfsr.engine.network.packet.common.world.entity.spawn.EntityPacketSpawnData;
import net.bfsr.engine.network.packet.common.world.entity.spawn.RigidBodySpawnData;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.engine.util.Side;
import net.bfsr.engine.world.ObjectPools;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
@RequiredArgsConstructor
public class GameLogic {
    private final AbstractGameLoop gameLoop;
    private final Side side;
    @Getter
    protected final Profiler profiler;
    @Getter
    protected final EventBus eventBus;

    @Setter
    @Getter
    private boolean isRunning;
    @Setter
    @Getter
    private boolean paused;

    /**
     * Queue of Runnable which will execute in next game logic update step
     *
     * @see GameLogic#update(int, double)
     */
    private final Queue<Runnable> futureTasks = new ConcurrentLinkedQueue<>();

    private final Int2ObjectMap<Logic> customLogic = new Int2ObjectOpenHashMap<>();

    private final ObjectPools objectPools = new ObjectPools();

    public void update(int frame, double time) {
        profiler.start("tasks");
        while (!futureTasks.isEmpty()) {
            futureTasks.poll().run();
        }
        profiler.end();
    }

    /**
     * Adds Runnable to queue which will execute in next game logic update step by main game thread.
     * This method is thread-safe.
     *
     * @param runnable the Runnable to execute
     */
    public void addFutureTask(Runnable runnable) {
        futureTasks.add(runnable);
    }

    public void registerLogic(int id, Logic logic) {
        customLogic.put(id, logic);
    }

    public <T> T getLogic(int id) {
        return (T) customLogic.get(id);
    }

    public boolean isServer() {
        return side.isServer();
    }

    public boolean isClient() {
        return side.isClient();
    }

    public <T> ObjectPool<T> addObjectPool(Class<T> objectClass, ObjectPool<T> objectPool) {
        objectPools.addPool(objectClass, objectPool);
        return objectPool;
    }

    public <T> ObjectPool<T> getObjectPool(Class<T> objectClass) {
        return objectPools.getPool(objectClass);
    }

    public EntityPacketSpawnData<?> getEntitySpawnData(int entityId) {
        return new RigidBodySpawnData<>();
    }

    public void setFrame(int frame) {
        gameLoop.setFrame(frame);
    }

    public void setTime(double gameTime) {
        gameLoop.setTime(gameTime);
    }

    public int getFrame() {
        return gameLoop.getFrame();
    }

    public double getTime() {
        return gameLoop.getTime();
    }

    public void shutdown() {
        isRunning = false;
    }

    public void clear() {
        objectPools.clear();
    }
}