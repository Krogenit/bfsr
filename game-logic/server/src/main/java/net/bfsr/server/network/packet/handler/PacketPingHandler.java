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
        long nanoTime = System.nanoTime();
        if (packet.getSide() == Side.CLIENT) {
            playerNetworkHandler.sendUDPPacket(new PacketPing(packet.getOriginalSentTime(), nanoTime, packet.getSide()));
        } else {
            playerNetworkHandler.setPing((nanoTime - packet.getOriginalSentTime()) / 2_000_000.0);
        }
    }
}