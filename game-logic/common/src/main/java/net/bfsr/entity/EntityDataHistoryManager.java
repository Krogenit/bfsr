package net.bfsr.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.event.entity.RigidBodyRemovedFromWorldEvent;
import net.bfsr.network.packet.common.entity.PacketWorldSnapshot;
import org.jetbrains.annotations.Nullable;

public class EntityDataHistoryManager {
    private static final long HISTORY_DURATION_MILLIS = 2500;
    private static final PositionHistory EMPTY_POSITION_HISTORY = new PositionHistory(0) {
        @Override
        public @Nullable TransformData get(double time) {
            return null;
        }
    };
    private static final EntityDataHistory<PacketWorldSnapshot.EntityData> EMPTY_DATA_HISTORY = new EntityDataHistory<>(0) {
        @Override
        public @Nullable PacketWorldSnapshot.EntityData get(double time) {
            return null;
        }
    };

    private final Int2ObjectMap<PositionHistory> positionHistoryMap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<EntityDataHistory<PacketWorldSnapshot.EntityData>> dataHistoryMap = new Int2ObjectOpenHashMap<>();

    EntityDataHistoryManager() {
        positionHistoryMap.defaultReturnValue(EMPTY_POSITION_HISTORY);
        dataHistoryMap.defaultReturnValue(EMPTY_DATA_HISTORY);
    }

    public void addData(PacketWorldSnapshot.EntityData entityData, double timestamp) {
        int id = entityData.getEntityId();
        positionHistoryMap.computeIfAbsent(id, key -> new PositionHistory(HISTORY_DURATION_MILLIS))
                .addPositionData(entityData.getX(), entityData.getY(), entityData.getSin(), entityData.getCos(), timestamp);
        dataHistoryMap.computeIfAbsent(id, key -> new EntityDataHistory<>(HISTORY_DURATION_MILLIS)).addData(entityData);
    }

    public TransformData getTransformData(int id, double timestamp) {
        return positionHistoryMap.get(id).get(timestamp);
    }

    public TransformData getFirstTransformData(int id) {
        return positionHistoryMap.get(id).getFirst();
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