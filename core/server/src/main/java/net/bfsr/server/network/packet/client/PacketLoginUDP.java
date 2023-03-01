package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.AsyncPacketIn;

import java.io.IOException;
import java.net.InetSocketAddress;

@Log4j2
public class PacketLoginUDP implements AsyncPacketIn {
    private String login;
    private byte[] digest;

    @Override
    public void read(ByteBuf data) throws IOException {
        login = ByteBufUtils.readString(data);
        data.readBytes(digest = new byte[data.readByte()]);
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        PlayerNetworkHandler networkHandler = MainServer.getInstance().getNetworkSystem().getHandler(login);
        if (networkHandler == null) {
            log.error("Network Handler not found for player {} {}", login, ctx.channel().remoteAddress());
            ctx.channel().close();
            return;
        }

        networkHandler.loginUDP(login, digest, ctx, remoteAddress);
    }
}
