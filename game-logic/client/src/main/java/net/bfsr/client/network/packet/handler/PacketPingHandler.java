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
        long nanoTime = System.nanoTime();
        if (packet.getSide() == Side.CLIENT) {
            long roundTripTimeNanos = nanoTime - packet.getOriginalSentTime();
            long pingNanos = roundTripTimeNanos / 2;
            double pingMillis = pingNanos / 1_000_000.0;
            long timeDiff = nanoTime - packet.getOtherSideHandleTime();
            long clientToServerTimeDiff = timeDiff - pingNanos;
            netHandler.addPingResult(pingMillis);
            netHandler.addClientToServerTimeDiffResult(clientToServerTimeDiff);
        } else {
            netHandler.sendPacketUDP(new PacketPing(packet.getOriginalSentTime(), nanoTime, packet.getSide()));
        }
    }
}