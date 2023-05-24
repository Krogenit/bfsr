package net.bfsr.client.network.packet.handler;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketKeepAlive;

import java.net.InetSocketAddress;

public class PacketKeepAliveHandler extends PacketHandler<PacketKeepAlive, NetworkSystem> {
    @Override
    public void handle(PacketKeepAlive packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        ctx.writeAndFlush(new PacketKeepAlive());
    }
}