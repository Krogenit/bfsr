package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import net.bfsr.engine.world.entity.EntityDataHistoryManager;

import java.net.InetSocketAddress;

@Log4j2
public class PacketWorldSnapshotHandler extends PacketHandler<PacketWorldSnapshot, NetworkSystem> {
    private final Client client = Client.get();

    @Override
    public void handle(PacketWorldSnapshot packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        double time = client.getTime();
        double serverTime = packet.getTime();
        double clientPrediction = client.getNetworkSystem().getAveragePing() * 1_000_000.0;
        if (time < serverTime || time > serverTime + clientPrediction) {
            log.info("Adjust client time and frame, frame diff: {}", packet.getFrame() - client.getFrame());

            client.setTime(serverTime);
            client.setFrame(packet.getFrame());
        }

        UnorderedArrayList<PacketWorldSnapshot.EntityData> entityDataList = packet.getEntityDataList();
        EntityDataHistoryManager historyManager = Client.get().getWorld().getEntityManager().getDataHistoryManager();
        for (int i = 0; i < entityDataList.size(); i++) {
            historyManager.addData(entityDataList.get(i), packet.getFrame());
        }
    }
}