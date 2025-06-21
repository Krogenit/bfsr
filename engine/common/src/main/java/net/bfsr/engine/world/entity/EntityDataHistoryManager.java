package net.bfsr.engine.world.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.entity.RigidBodyRemovedFromWorldEvent;
import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import net.bfsr.engine.network.sync.DataHistory;
import org.jetbrains.annotations.Nullable;

public class EntityDataHistoryManager {
    private static final long HISTORY_DURATION_MILLIS = 2500;
    private static final EntityPositionHistory EMPTY_POSITION_HISTORY = new EntityPositionHistory(0) {
        @Override
        public @Nullable TransformData get(double time) {
            return null;
        }
    };
    private static final DataHistory<PacketWorldSnapshot.EntityData> EMPTY_DATA_HISTORY = new DataHistory<>(0) {
        @Override
        public @Nullable PacketWorldSnapshot.EntityData get(double time) {
            return null;
        }
    };

    private final Int2ObjectMap<EntityPositionHistory> positionHistoryMap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<DataHistory<PacketWorldSnapshot.EntityData>> dataHistoryMap = new Int2ObjectOpenHashMap<>();

    public EntityDataHistoryManager() {
        positionHistoryMap.defaultReturnValue(EMPTY_POSITION_HISTORY);
        dataHistoryMap.defaultReturnValue(EMPTY_DATA_HISTORY);
    }

    public void addData(PacketWorldSnapshot.EntityData entityData, double timestamp) {
        int id = entityData.getEntityId();
        addPositionData(id, entityData.getX(), entityData.getY(), entityData.getSin(), entityData.getCos(), timestamp);
        dataHistoryMap.computeIfAbsent(id, key -> new DataHistory<>(HISTORY_DURATION_MILLIS)).addData(entityData);
    }

    public void addPositionData(int id, float x, float y, float sin, float cos, double timestamp) {
        positionHistoryMap.computeIfAbsent(id, key -> new EntityPositionHistory(HISTORY_DURATION_MILLIS))
                .addPositionData(x, y, sin, cos, timestamp);
    }

    public TransformData getTransformData(int id, double timestamp) {
        return positionHistoryMap.get(id).get(timestamp);
    }

    public TransformData getFirstTransformData(int id) {
        return positionHistoryMap.get(id).getFirst();
    }

    public TransformData getAndRemoveFirstTransformData(int id) {
        return positionHistoryMap.get(id).getAndRemoveFirst();
    }

    public PacketWorldSnapshot.EntityData getAndRemoveFirstData(int id) {
        return dataHistoryMap.get(id).getAndRemoveFirst();
    }

    public PacketWorldSnapshot.EntityData getData(int id, double timestamp) {
        return dataHistoryMap.get(id).get(timestamp);
    }

    @EventHandler
    public EventListener<RigidBodyRemovedFromWorldEvent> rigidBodyDeathEvent() {
        return event -> {
            int id = event.rigidBody().getId();
            positionHistoryMap.remove(id);
            dataHistoryMap.remove(id);
        };
    }

    public void clear() {
        positionHistoryMap.clear();
        dataHistoryMap.clear();
    }
}