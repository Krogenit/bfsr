package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.login.PacketJoinGame;
import net.bfsr.network.packet.server.login.PacketLoginUDPSuccess;

import java.net.InetSocketAddress;

public class PacketLoginUDPSuccessHandler extends PacketHandler<PacketLoginUDPSuccess, NetworkSystem> {
    @Override
    public void handle(PacketLoginUDPSuccess packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        ctx.writeAndFlush(new PacketJoinGame());
    }
}