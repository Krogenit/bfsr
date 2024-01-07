package net.bfsr.server.network.packet.handler;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.util.Side;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketPing;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketPingHandler extends PacketHandler<PacketPing, PlayerNetworkHandler> {
    @Override
    public void handle(PacketPing packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        if (packet.getSide() == Side.CLIENT) {
            playerNetworkHandler.sendUDPPacket(new PacketPing(
                    System.nanoTime() - (playerNetworkHandler.getHandshakeClientTime() + packet.getOneWayTime()),
                    packet.getOriginalSentTime(), System.nanoTime(), packet.getSide()));
        } else {
            playerNetworkHandler.setPing((System.nanoTime() - packet.getOriginalSentTime()) / 2_000_000.0);
        }
    }
}