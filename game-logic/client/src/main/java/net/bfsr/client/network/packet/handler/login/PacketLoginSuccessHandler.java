package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.login.PacketJoinGame;
import net.bfsr.network.packet.server.login.PacketLoginSuccess;

import java.net.InetSocketAddress;

@Log4j2
public class PacketLoginSuccessHandler extends PacketHandler<PacketLoginSuccess, NetworkSystem> {
    @Override
    public void handle(PacketLoginSuccess packet, NetworkSystem netHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        ctx.writeAndFlush(new PacketJoinGame());
    }
}