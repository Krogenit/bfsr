package net.bfsr.server.network;

import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.common.entity.PacketWorldSnapshot;
import net.bfsr.server.ServerGameLogic;

import java.util.ArrayList;
import java.util.List;

public class EntitySyncManager {
    private static final int MAX_ENTITY_DATA_IN_PACKET = 32;

    public void sendEntitiesToClients(List<? extends RigidBody<?>> entities, double time) {
        List<PacketWorldSnapshot.EntityData> entityDataList = new ArrayList<>(
                Math.min(MAX_ENTITY_DATA_IN_PACKET, entities.size()));

        for (int i = 0; i < entities.size(); i++) {
            RigidBody<?> rigidBody = entities.get(i);
            entityDataList.add(new PacketWorldSnapshot.EntityData(rigidBody, time));

            if (entityDataList.size() == MAX_ENTITY_DATA_IN_PACKET) {
                ServerGameLogic.getNetwork().sendUDPPacketToAll(new PacketWorldSnapshot(entityDataList, time));
                int newCount = entities.size() - (i + 1);
                if (newCount > 0) {
                    entityDataList = new ArrayList<>(Math.min(MAX_ENTITY_DATA_IN_PACKET, newCount));
                }
            }
        }

        if (entityDataList.size() > 0) {
            ServerGameLogic.getNetwork().sendUDPPacketToAll(new PacketWorldSnapshot(entityDataList, time));
        }
    }
}