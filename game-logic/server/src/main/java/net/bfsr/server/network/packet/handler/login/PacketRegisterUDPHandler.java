package net.bfsr.server.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketRegisterUDP;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

@Log4j2
public class PacketRegisterUDPHandler extends PacketHandler<PacketRegisterUDP, PlayerNetworkHandler> {
    @Override
    public void handle(PacketRegisterUDP packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        PlayerNetworkHandler handler = ServerGameLogic.getInstance().getNetworkSystem().getHandler(packet.getConnectionId());
        if (handler == null) {
            log.error("Handler not found for connection id {}", packet.getConnectionId());
            ctx.channel().close();
            return;
        }

        if (handler.getDatagramChannel() != null) {
            log.error("Datagram channel already registered for connection id {}", packet.getConnectionId());
            ctx.channel().close();
            return;
        }

        handler.setDatagramChannel((DatagramChannel) ctx.channel(), remoteAddress);
        log.info("Successfully registered datagram channel for connection id {}", packet.getConnectionId());

        handler.sendTCPPacket(new PacketRegisterUDP());
    }
}