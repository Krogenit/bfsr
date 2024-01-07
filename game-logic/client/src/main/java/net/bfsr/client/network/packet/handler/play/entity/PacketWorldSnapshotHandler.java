package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.entity.PacketWorldSnapshot;

import java.net.InetSocketAddress;
import java.util.List;

public class PacketWorldSnapshotHandler extends PacketHandler<PacketWorldSnapshot, NetworkSystem> {
    @Override
    public void handle(PacketWorldSnapshot packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        List<PacketWorldSnapshot.EntityData> entityDataList = packet.getEntityDataList();
        for (int i = 0; i < entityDataList.size(); i++) {
            PacketWorldSnapshot.EntityData entityData = entityDataList.get(i);
            RigidBody<?> entity = Core.get().getWorld().getEntityById(entityData.getEntityId());
            if (entity != null) {
                entity.addPositionData(entityData, packet.getTimestamp());
            }
        }
    }
}