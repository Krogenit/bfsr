package net.bfsr.engine.network.packet;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.NetworkHandler;

import java.net.InetSocketAddress;

public abstract class PacketHandler<PACKET extends Packet, NET_HANDLER extends NetworkHandler> {
    public abstract void handle(PACKET packet, NET_HANDLER netHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress);

    public void handle(PACKET packet, NET_HANDLER netHandler, ChannelHandlerContext ctx) {
        handle(packet, netHandler, ctx, null);
    }

    public void handle(PACKET packet, NET_HANDLER netHandler) {
        handle(packet, netHandler, null, null);
    }
}