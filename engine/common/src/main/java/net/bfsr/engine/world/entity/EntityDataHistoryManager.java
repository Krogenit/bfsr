package net.bfsr.engine.world.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.entity.RigidBodyRemovedFromWorldEvent;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

public class EntityDataHistoryManager {
    private static final EntityPositionHistory EMPTY_POSITION_HISTORY = new EntityPositionHistory(0) {
        @Override
        public @Nullable TransformData get(int frame) {
            return null;
        }
    };
    private static final EntityVelocityHistory EMPTY_VELOCITY_HISTORY = new EntityVelocityHistory(0) {
        @Override
        public @Nullable VelocityData get(int frame) {
            return null;
        }
    };

    private final Int2ObjectMap<EntityPositionHistory> positionHistoryMap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<EntityVelocityHistory> velocityHistoryMap = new Int2ObjectOpenHashMap<>();

    public EntityDataHistoryManager() {
        positionHistoryMap.defaultReturnValue(EMPTY_POSITION_HISTORY);
        velocityHistoryMap.defaultReturnValue(EMPTY_VELOCITY_HISTORY);
    }

    public void addData(PacketWorldSnapshot.EntityData entityData, int frame) {
        int id = entityData.getEntityId();
        addPositionData(id, entityData.getX(), entityData.getY(), entityData.getSin(), entityData.getCos(), frame);
        addVelocityData(id, entityData.getVelocity(), entityData.getAngularVelocity(), frame);
    }

    public void addPositionData(int id, float x, float y, float sin, float cos, int frame) {
        synchronized (positionHistoryMap) {
            positionHistoryMap.computeIfAbsent(id, key -> new EntityPositionHistory(NetworkHandler.GLOBAL_HISTORY_LENGTH_FRAMES))
                    .addData(x, y, sin, cos, frame);
        }
    }

    private void addVelocityData(int id, Vector2f velocity, float angularVelocity, int frame) {
        synchronized (velocityHistoryMap) {
            velocityHistoryMap.computeIfAbsent(id, key -> new EntityVelocityHistory(NetworkHandler.GLOBAL_HISTORY_LENGTH_FRAMES))
                    .addData(velocity, angularVelocity, frame);
        }
    }

    public TransformData getTransformData(int id, int frame) {
        synchronized (positionHistoryMap) {
            return positionHistoryMap.get(id).getInterpolated(frame);
        }
    }

    public TransformData getFirstTransformData(int id) {
        synchronized (positionHistoryMap) {
            return positionHistoryMap.get(id).getFirst();
        }
    }

    public TransformData getAndRemoveFirstTransformData(int id) {
        synchronized (positionHistoryMap) {
            return positionHistoryMap.get(id).getAndRemoveFirst();
        }
    }

    public VelocityData getAndRemoveFirstData(int id) {
        synchronized (velocityHistoryMap) {
            return velocityHistoryMap.get(id).getAndRemoveFirst();
        }
    }

    public VelocityData getData(int id, int frame) {
        synchronized (velocityHistoryMap) {
            return velocityHistoryMap.get(id).getInterpolated(frame);
        }
    }

    @EventHandler
    public EventListener<RigidBodyRemovedFromWorldEvent> rigidBodyDeathEvent() {
        return event -> {
            int id = event.rigidBody().getId();
            synchronized (positionHistoryMap) {
                positionHistoryMap.remove(id);
            }
            synchronized (velocityHistoryMap) {
                velocityHistoryMap.remove(id);
            }
        };
    }

    public void clear() {
        synchronized (positionHistoryMap) {
            positionHistoryMap.clear();
        }
        synchronized (velocityHistoryMap) {
            velocityHistoryMap.clear();
        }
    }
}