package net.bfsr.server.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketLoginUDP;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

@Log4j2
public class PacketLoginUDPHandler extends PacketHandler<PacketLoginUDP, PlayerNetworkHandler> {
    @Override
    public void handle(PacketLoginUDP packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        PlayerNetworkHandler networkHandler = ServerGameLogic.getInstance().getNetworkSystem().getHandler(packet.getLogin());
        if (networkHandler == null) {
            log.error("Network Handler not found for player {} {}", packet.getLogin(), ctx.channel().remoteAddress());
            ctx.channel().close();
            return;
        }

        networkHandler.loginUDP(packet.getLogin(), packet.getDigest(), ctx, remoteAddress);
    }
}