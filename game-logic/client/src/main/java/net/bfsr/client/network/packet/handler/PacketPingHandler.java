package net.bfsr.client.network.packet.handler;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.common.PacketPing;
import net.bfsr.engine.util.Side;

import java.net.InetSocketAddress;

public class PacketPingHandler extends PacketHandler<PacketPing, NetworkSystem> {
    @Override
    public void handle(PacketPing packet, NetworkSystem netHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        if (packet.getSide() == Side.CLIENT) {
            long roundTripTimeNanos = System.nanoTime() - packet.getOriginalSentTime();
            long pingNanos = roundTripTimeNanos / 2;
            double pingMillis = pingNanos / 1_000_000.0;
            netHandler.addPingResult(pingMillis);
        } else {
            netHandler.sendPacketUDP(new PacketPing(packet.getOriginalSentTime(), packet.getSide()));
        }
    }
}