package net.bfsr.client.network.packet.handler.play.entity;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.entity.GameObject;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.PacketRemoveObject;

import java.net.InetSocketAddress;

public class PacketRemoveObjectHandler extends PacketHandler<PacketRemoveObject, NetworkSystem> {
    @Override
    public void handle(PacketRemoveObject packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = Client.get().getWorld().getEntityById(packet.getId());
        if (obj != null) {
            obj.setDead();
        }
    }
}