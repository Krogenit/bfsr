package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.login.PacketLoginTCPSuccess;

import java.net.InetSocketAddress;

@Log4j2
public class PacketLoginTCPSuccessHandler extends PacketHandler<PacketLoginTCPSuccess, NetworkSystem> {
    @Override
    public void handle(PacketLoginTCPSuccess packet, NetworkSystem netHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        log.debug("Login success. Connecting via UDP");
        Core.get().establishUDPConnection(packet.getDigest());
        packet.clearDigest();
    }
}