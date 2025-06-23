package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import net.bfsr.engine.world.entity.EntityDataHistoryManager;

import java.net.InetSocketAddress;

public class PacketWorldSnapshotHandler extends PacketHandler<PacketWorldSnapshot, NetworkSystem> {
    @Override
    public void handle(PacketWorldSnapshot packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        UnorderedArrayList<PacketWorldSnapshot.EntityData> entityDataList = packet.getEntityDataList();
        EntityDataHistoryManager historyManager = Client.get().getWorld().getEntityManager().getDataHistoryManager();
        for (int i = 0; i < entityDataList.size(); i++) {
            historyManager.addData(entityDataList.get(i), packet.getTick());
        }
    }
}