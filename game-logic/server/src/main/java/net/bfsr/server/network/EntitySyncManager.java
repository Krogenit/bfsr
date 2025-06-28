package net.bfsr.server.network;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.server.player.Player;

@RequiredArgsConstructor
public class EntitySyncManager {
    private static final int MAX_ENTITY_DATA_IN_PACKET = 32;

    private final NetworkSystem network;
    private final UnorderedArrayList<PacketWorldSnapshot.EntityData> entityDataList = new UnorderedArrayList<>(128);

    public void addToSyncQueue(RigidBody entity, int frame, double time, Player player) {
        entityDataList.add(new PacketWorldSnapshot.EntityData(entity));

        if (entityDataList.size() == MAX_ENTITY_DATA_IN_PACKET) {
            network.sendUDPPacketTo(new PacketWorldSnapshot(entityDataList, frame, time), player);
            entityDataList.clear();
        }
    }

    public void flush(Player player, int frame, double time) {
        if (entityDataList.size() > 0) {
            network.sendUDPPacketTo(new PacketWorldSnapshot(entityDataList, frame, time), player);
            entityDataList.clear();
        }
    }

    public void clear() {
        entityDataList.clear();
    }
}