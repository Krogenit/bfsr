package net.bfsr.server.network;

import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.common.entity.PacketWorldSnapshot;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.player.Player;

public class EntitySyncManager {
    private static final int MAX_ENTITY_DATA_IN_PACKET = 32;

    private final NetworkSystem network = ServerGameLogic.getNetwork();
    private final UnorderedArrayList<PacketWorldSnapshot.EntityData> entityDataList = new UnorderedArrayList<>(128);

    public void addToSyncQueue(RigidBody<?> entity, double time, Player player) {
        entityDataList.add(new PacketWorldSnapshot.EntityData(entity, time));

        if (entityDataList.size() == MAX_ENTITY_DATA_IN_PACKET) {
            network.sendUDPPacketTo(new PacketWorldSnapshot(entityDataList, time), player);
            entityDataList.clear();
        }
    }

    public void flush(Player player, double time) {
        if (entityDataList.size() > 0) {
            network.sendUDPPacketTo(new PacketWorldSnapshot(entityDataList, time), player);
            entityDataList.clear();
        }
    }

    public void clear() {
        entityDataList.clear();
    }
}