package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.common.world.entity.PacketEntityRemove;
import net.bfsr.engine.world.entity.GameObject;

import java.net.InetSocketAddress;

public class PacketRemoveObjectHandler extends PacketHandler<PacketEntityRemove, NetworkSystem> {
    @Override
    public void handle(PacketEntityRemove packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = Client.get().getWorld().getEntityById(packet.getId());
        if (obj != null) {
            obj.setDead();
        }
    }
}