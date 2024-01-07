package net.bfsr.server.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketRegisterUDP;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

@Log4j2
public class PacketRegisterUDPHandler extends PacketHandler<PacketRegisterUDP, PlayerNetworkHandler> {
    private final NetworkSystem networkSystem = ServerGameLogic.getNetwork();

    @Override
    public void handle(PacketRegisterUDP packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        PlayerNetworkHandler handler = networkSystem.getHandler(packet.getConnectionId());
        if (handler == null) {
            log.error("Handler not found for connection id {}", packet.getConnectionId());
            ctx.channel().close();
            return;
        }

        if (handler.getRemoteAddress() != null) {
            log.error("UDP connection already registered for id {}", packet.getConnectionId());
            ctx.channel().close();
            return;
        }

        networkSystem.registerUDPRemoteAddress(handler, remoteAddress);
        log.info("Successfully registered datagram channel {} for connection id {}", remoteAddress, packet.getConnectionId());

        handler.sendTCPPacket(new PacketRegisterUDP());
    }
}