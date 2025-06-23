package net.bfsr.engine.world.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.entity.RigidBodyRemovedFromWorldEvent;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import net.bfsr.engine.network.sync.DataTickHistory;
import org.jetbrains.annotations.Nullable;

public class EntityDataHistoryManager {
    private static final EntityPositionHistory EMPTY_POSITION_HISTORY = new EntityPositionHistory(0) {
        @Override
        public @Nullable TransformData get(int tick) {
            return null;
        }
    };
    private static final DataTickHistory<PacketWorldSnapshot.EntityData> EMPTY_DATA_HISTORY = new DataTickHistory<>(0) {
        @Override
        public @Nullable PacketWorldSnapshot.EntityData get(int tick) {
            return null;
        }
    };

    private final Int2ObjectMap<EntityPositionHistory> positionHistoryMap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<DataTickHistory<PacketWorldSnapshot.EntityData>> dataHistoryMap = new Int2ObjectOpenHashMap<>();

    public EntityDataHistoryManager() {
        positionHistoryMap.defaultReturnValue(EMPTY_POSITION_HISTORY);
        dataHistoryMap.defaultReturnValue(EMPTY_DATA_HISTORY);
    }

    public void addData(PacketWorldSnapshot.EntityData entityData, int tick) {
        int id = entityData.getEntityId();
        addPositionData(id, entityData.getX(), entityData.getY(), entityData.getSin(), entityData.getCos(), tick);
        dataHistoryMap.computeIfAbsent(id, key -> new DataTickHistory<>(NetworkHandler.GLOBAL_HISTORY_LENGTH_MILLIS)).addData(entityData);
    }

    public void addPositionData(int id, float x, float y, float sin, float cos, int tick) {
        positionHistoryMap.computeIfAbsent(id, key -> new EntityPositionHistory(NetworkHandler.GLOBAL_HISTORY_LENGTH_MILLIS))
                .addPositionData(x, y, sin, cos, tick);
    }

    public TransformData getTransformData(int id, int tick) {
        return positionHistoryMap.get(id).get(tick);
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

    public PacketWorldSnapshot.EntityData getData(int id, int tick) {
        return dataHistoryMap.get(id).get(tick);
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